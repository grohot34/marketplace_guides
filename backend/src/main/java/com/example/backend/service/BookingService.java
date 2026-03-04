package com.example.backend.service;

import com.example.backend.dto.BookingDto;
import com.example.backend.dto.CreateBookingRequest;
import com.example.backend.dto.OrderEvent;
import com.example.backend.model.Booking;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final NotificationService notificationService;

    @Value("${kafka.topic.order-events:order-events}")
    private String orderEventsTopic;

    @Cacheable(value = "bookings", key = "'all'")
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "bookings", key = "#id")
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return convertToDto(booking);
    }

    public List<BookingDto> getBookingsByCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return bookingRepository.findByCustomer(customer).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByGuide(Long guideId) {
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));
        return bookingRepository.findByTour_Guide(guide).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByTour(Long tourId) {
        return bookingRepository.findByTourId(tourId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "bookings", key = "'status_' + #status")
    public List<BookingDto> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == status)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public BookingDto createBooking(Long customerId, CreateBookingRequest request) {
        try {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

            Tour tour = tourRepository.findById(request.getTourId())
                    .orElseThrow(() -> new RuntimeException("Tour not found with id: " + request.getTourId()));

            if (!tour.getActive()) {
                throw new RuntimeException("Tour is not available");
            }

            if (request.getTourDateTime() == null) {
                throw new RuntimeException("Tour date time is required");
            }

            if (request.getNumberOfParticipants() == null || request.getNumberOfParticipants() < 1) {
                throw new RuntimeException("Number of participants must be at least 1");
            }

            if (request.getNumberOfParticipants() > tour.getMaxParticipants()) {
                throw new RuntimeException("Number of participants exceeds maximum allowed (" + tour.getMaxParticipants() + ")");
            }

            // Check availability
            Long existingBookings = bookingRepository.countActiveBookingsForTourAndDateTime(tour, request.getTourDateTime());
            if (existingBookings + request.getNumberOfParticipants() > tour.getMaxParticipants()) {
                throw new RuntimeException("Not enough available spots for this tour date");
            }

            BigDecimal totalPrice = tour.getPricePerPerson().multiply(BigDecimal.valueOf(request.getNumberOfParticipants()));

            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setTour(tour);
            booking.setTourDateTime(request.getTourDateTime());
            booking.setNumberOfParticipants(request.getNumberOfParticipants());
            booking.setContactPhone(request.getContactPhone() != null ? request.getContactPhone() : customer.getPhone());
            booking.setSpecialRequests(request.getSpecialRequests());
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setTotalPrice(totalPrice);

            booking = bookingRepository.save(booking);

            try {
                OrderEvent event = new OrderEvent(
                        booking.getId(),
                        customer.getId(),
                        tour.getId(),
                        Booking.BookingStatus.PENDING.name(),
                        LocalDateTime.now(),
                        "Booking created successfully"
                );
                kafkaTemplate.send(orderEventsTopic, event);
            } catch (Exception e) {
                System.err.println("Failed to send Kafka event: " + e.getMessage());
            }

            try {
                notificationService.sendNotification(
                        customer.getId(),
                        "BOOKING_CREATED",
                        "Booking Created",
                        "Your booking for " + tour.getTitle() + " has been created successfully"
                );

                notificationService.sendNotification(
                        tour.getGuide().getId(),
                        "NEW_BOOKING",
                        "New Booking",
                        "You have a new booking for " + tour.getTitle()
                );
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }

            return convertToDto(booking);
        } catch (RuntimeException e) {
            System.err.println("Error in createBooking: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error in createBooking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public BookingDto updateBookingStatus(Long id, Booking.BookingStatus status, Long guideId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (guideId != null && booking.getGuide() != null) {
            if (!booking.getGuide().getId().equals(guideId)) {
                throw new RuntimeException("Guide can only update status of their own bookings");
            }
        }

        booking.setStatus(status);
        if (status == Booking.BookingStatus.CONFIRMED) {
            booking.setConfirmedAt(LocalDateTime.now());
        } else if (status == Booking.BookingStatus.COMPLETED) {
            booking.setCompletedAt(LocalDateTime.now());
        } else if (status == Booking.BookingStatus.CANCELLED) {
            booking.setCancelledAt(LocalDateTime.now());
        }

        booking = bookingRepository.save(booking);

        try {
            OrderEvent event = new OrderEvent(
                    booking.getId(),
                    booking.getCustomer().getId(),
                    booking.getTour().getId(),
                    booking.getStatus().name(),
                    LocalDateTime.now(),
                    "Booking status updated to " + status
            );
            kafkaTemplate.send(orderEventsTopic, event);
        } catch (Exception e) {
            System.err.println("Failed to send Kafka event: " + e.getMessage());
        }

        try {
            notificationService.sendNotification(
                    booking.getCustomer().getId(),
                    "BOOKING_STATUS_UPDATED",
                    "Booking Status Updated",
                    "Your booking status has been updated to " + status
            );
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }

        return convertToDto(booking);
    }

    @Transactional
    public BookingDto updateBookingStatus(Long id, Booking.BookingStatus status) {
        return updateBookingStatus(id, status, null);
    }

    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public void cancelBooking(Long id, Long userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        boolean isCustomer = booking.getCustomer().getId().equals(userId);
        boolean isGuide = booking.getGuide() != null && booking.getGuide().getId().equals(userId);
        if (!isAdmin && !isCustomer && !isGuide) {
            throw new RuntimeException("Only the customer who made the booking, the guide of the tour, or admin can cancel it");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        try {
            OrderEvent event = new OrderEvent(
                    booking.getId(),
                    booking.getCustomer().getId(),
                    booking.getTour().getId(),
                    Booking.BookingStatus.CANCELLED.name(),
                    LocalDateTime.now(),
                    "Booking cancelled"
            );
            kafkaTemplate.send(orderEventsTopic, event);
        } catch (Exception e) {
            System.err.println("Failed to send Kafka event: " + e.getMessage());
        }

        try {
            notificationService.sendNotification(
                    booking.getGuide().getId(),
                    "BOOKING_CANCELLED",
                    "Booking Cancelled",
                    "Booking #" + booking.getId() + " has been cancelled"
            );
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "bookings", allEntries = true)
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        try {
            OrderEvent event = new OrderEvent(
                    booking.getId(),
                    booking.getCustomer().getId(),
                    booking.getTour().getId(),
                    booking.getStatus().name(),
                    LocalDateTime.now(),
                    "Booking deleted by admin"
            );
            kafkaTemplate.send(orderEventsTopic, event);
        } catch (Exception e) {
            System.err.println("Failed to send Kafka event: " + e.getMessage());
        }

        bookingRepository.deleteById(id);
    }

    private BookingDto convertToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setCustomerId(booking.getCustomer().getId());
        dto.setCustomerName(booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName());
        dto.setTourId(booking.getTour().getId());
        dto.setTourTitle(booking.getTour().getTitle());
        dto.setGuideId(booking.getGuide().getId());
        dto.setGuideName(booking.getGuide().getFirstName() + " " + booking.getGuide().getLastName());
        dto.setTourDateTime(booking.getTourDateTime());
        dto.setNumberOfParticipants(booking.getNumberOfParticipants());
        dto.setContactPhone(booking.getContactPhone());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setStatus(booking.getStatus());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setConfirmedAt(booking.getConfirmedAt());
        dto.setCompletedAt(booking.getCompletedAt());
        dto.setCancelledAt(booking.getCancelledAt());
        dto.setPaid(Boolean.TRUE.equals(booking.getPaid()));
        reviewRepository.findByBooking_Id(booking.getId()).stream()
                .findFirst()
                .ifPresent(r -> {
                    dto.setHasReview(true);
                    dto.setReviewId(r.getId());
                });
        if (dto.getHasReview() == null) dto.setHasReview(false);

        return dto;
    }
}
