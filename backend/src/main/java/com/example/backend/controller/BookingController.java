package com.example.backend.controller;

import com.example.backend.dto.BookingDto;
import com.example.backend.dto.CreateBookingRequest;
import com.example.backend.model.Booking;
import com.example.backend.model.User;
import com.example.backend.service.BookingService;
import com.example.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtil securityUtil;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user bookings (as customer)")
    public ResponseEntity<List<BookingDto>> getMyBookings(Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(bookingService.getBookingsByCustomer(userId));
    }

    @GetMapping("/my-guide-bookings")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Get bookings for guide's tours")
    public ResponseEntity<List<BookingDto>> getMyGuideBookings(Authentication authentication) {
        Long guideId = securityUtil.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(bookingService.getBookingsByGuide(guideId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get bookings by customer (Admin only)")
    public ResponseEntity<List<BookingDto>> getBookingsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(bookingService.getBookingsByCustomer(customerId));
    }

    @GetMapping("/guide/{guideId}")
    @Operation(summary = "Get bookings by guide")
    public ResponseEntity<List<BookingDto>> getBookingsByGuide(@PathVariable Long guideId) {
        return ResponseEntity.ok(bookingService.getBookingsByGuide(guideId));
    }

    @GetMapping("/tour/{tourId}")
    @Operation(summary = "Get bookings by tour")
    public ResponseEntity<List<BookingDto>> getBookingsByTour(@PathVariable Long tourId) {
        return ResponseEntity.ok(bookingService.getBookingsByTour(tourId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status")
    public ResponseEntity<List<BookingDto>> getBookingsByStatus(@PathVariable Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'GUIDE')")
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        Long customerId = securityUtil.getUserIdFromAuthentication(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(customerId, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Update booking status")
    public ResponseEntity<BookingDto> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam Booking.BookingStatus status,
            Authentication authentication) {
        Long guideId = null;
        User.Role role = securityUtil.getUserRoleFromAuthentication(authentication);
        
        if (role == User.Role.GUIDE) {
            guideId = securityUtil.getUserIdFromAuthentication(authentication);
        }
        
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status, guideId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'GUIDE', 'ADMIN')")
    @Operation(summary = "Cancel booking (customer, guide of the tour, or admin)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, Authentication authentication) {
        Long userId = securityUtil.getUserIdFromAuthentication(authentication);
        User.Role role = securityUtil.getUserRoleFromAuthentication(authentication);
        bookingService.cancelBooking(id, userId, role == User.Role.ADMIN);
        return ResponseEntity.noContent().build();
    }
}
