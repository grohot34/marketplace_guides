package com.example.backend.controller;

import com.example.backend.service.StripePaymentService;
import com.example.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Stripe payment endpoints")
public class PaymentController {

    private final StripePaymentService stripePaymentService;
    private final SecurityUtil securityUtil;

    @Value("${app.frontend.url:http://localhost:3001}")
    private String frontendUrl;

    @PostMapping("/checkout-session")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'GUIDE')")
    @Operation(summary = "Create Stripe Checkout session for a booking")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody CreateCheckoutRequest request,
            Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        String successUrl = request.getSuccessUrl() != null && !request.getSuccessUrl().isBlank()
                ? request.getSuccessUrl()
                : frontendUrl + "/bookings?payment=success";
        String cancelUrl = request.getCancelUrl() != null && !request.getCancelUrl().isBlank()
                ? request.getCancelUrl()
                : frontendUrl + "/bookings?payment=cancelled";
        String url = stripePaymentService.createCheckoutSession(request.getBookingId(), successUrl, cancelUrl);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/confirm-session")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'GUIDE')")
    @Operation(summary = "Confirm payment after return from Stripe (sync paid status)")
    public ResponseEntity<Void> confirmSession(@RequestParam String session_id, Authentication authentication) {
        stripePaymentService.confirmPaymentBySessionId(session_id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook (do not call manually)")
    public ResponseEntity<String> webhook(HttpServletRequest request, @RequestBody String payload) {
        String signature = request.getHeader("Stripe-Signature");
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature");
        }
        stripePaymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    public static class CreateCheckoutRequest {
        private Long bookingId;
        private String successUrl;
        private String cancelUrl;
    }
}
