package com.example.backend.service;

import com.example.backend.model.Booking;
import com.example.backend.repository.BookingRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final BookingRepository bookingRepository;

    @Value("${stripe.api.secret-key:}")
    private String secretKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    /** Курс BYN → EUR для оплаты (Stripe не поддерживает BYN). Например 0.28 = 1 BYN ≈ 0.28 EUR */
    @Value("${stripe.byn-to-eur-rate:0.28}")
    private double bynToEurRate;

    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    /**
     * Создаёт сессию Stripe Checkout для оплаты бронирования.
     * Сумма в BYN конвертируется в EUR по курсу (Stripe не поддерживает BYN); в описании указываем сумму в BYN.
     */
    public String createCheckoutSession(Long bookingId, String successUrl, String cancelUrl) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not configured (stripe.api.secret-key)");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (Boolean.TRUE.equals(booking.getPaid())) {
            throw new IllegalStateException("Booking is already paid");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for cancelled booking");
        }

        BigDecimal total = booking.getTotalPrice();
        // Stripe не поддерживает BYN — списываем в EUR по курсу (85 BYN × rate = X EUR в центах)
        long amountEurCents = total.multiply(BigDecimal.valueOf(bynToEurRate)).multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        if (amountEurCents < 50) {
            amountEurCents = 50; // минимум 0.50 EUR
        }

        String tourTitle = booking.getTour() != null ? booking.getTour().getTitle() : "Бронирование #" + bookingId;
        String lineItemName = String.format("Экскурсия: %s (%s BYN)", tourTitle, total.setScale(0, java.math.RoundingMode.HALF_UP));

        String successUrlWithSession = successUrl + (successUrl.contains("?") ? "&" : "?") + "session_id={CHECKOUT_SESSION_ID}";
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrlWithSession)
                .setCancelUrl(cancelUrl)
                .putMetadata("bookingId", bookingId.toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(amountEurCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(lineItemName)
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            log.error("Stripe Checkout Session create failed", e);
            throw new RuntimeException("Failed to create payment session: " + e.getMessage());
        }
    }

    /**
     * Подтверждает оплату по session_id (вызов с фронта после редиректа с Stripe).
     * Если вебхук не сработал (например, локальная разработка), состояние обновится при возврате пользователя.
     */
    public void confirmPaymentBySessionId(String sessionId) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not configured");
        }
        try {
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.retrieve(sessionId);
            if (!"paid".equals(session.getPaymentStatus())) {
                throw new RuntimeException("Session is not paid: " + session.getPaymentStatus());
            }
            String bookingIdStr = session.getMetadata() != null ? session.getMetadata().get("bookingId") : null;
            if (bookingIdStr == null) {
                throw new RuntimeException("No bookingId in session metadata");
            }
            Long bookingId = Long.parseLong(bookingIdStr);
            Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
            markBookingPaid(booking, session.getId());
        } catch (Exception e) {
            log.warn("Confirm payment by session failed: {}", e.getMessage());
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }

    private void markBookingPaid(Booking booking, String stripeSessionId) {
        if (Boolean.TRUE.equals(booking.getPaid())) {
            return;
        }
        booking.setPaid(true);
        booking.setStripePaymentId(stripeSessionId);
        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            booking.setConfirmedAt(java.time.LocalDateTime.now());
        }
        bookingRepository.save(booking);
        log.info("Booking {} marked as paid and confirmed", booking.getId());
    }

    /**
     * Обработка вебхука Stripe (checkout.session.completed).
     */
    public void handleWebhook(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed", e);
            throw new RuntimeException("Invalid webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer data = event.getDataObjectDeserializer();
            StripeObject stripeObject = data.getObject().orElse(null);
            if (stripeObject instanceof com.stripe.model.checkout.Session session) {
                String bookingIdStr = session.getMetadata() != null ? session.getMetadata().get("bookingId") : null;
                if (bookingIdStr != null) {
                    try {
                        Long bookingId = Long.parseLong(bookingIdStr);
                        Booking booking = bookingRepository.findById(bookingId).orElse(null);
                        if (booking != null) {
                            markBookingPaid(booking, session.getId());
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid bookingId in Stripe session metadata: {}", bookingIdStr);
                    }
                }
            }
        }
    }
}
