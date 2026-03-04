package com.example.backend.service;

import com.example.backend.dto.OrderEvent;
import com.example.backend.model.Booking;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService unit tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private User customer;
    private User guide;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingService, "orderEventsTopic", "order-events");
        customer = new User();
        customer.setId(100L);
        guide = new User();
        guide.setId(200L);
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setGuide(guide);
        booking = new Booking();
        booking.setId(10L);
        booking.setCustomer(customer);
        booking.setTour(tour);
        booking.setStatus(Booking.BookingStatus.PENDING);
    }

    @Test
    void cancelBooking_whenNotCustomerNorGuideNorAdmin_throws() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        long otherUserId = 999L;

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, otherUserId, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only the customer");
    }

    @Test
    void cancelBooking_whenCustomer_cancelsSuccessfully() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(10L, 100L, false);

        verify(bookingRepository).save(booking);
        assert (booking.getStatus() == Booking.BookingStatus.CANCELLED);
        assert (booking.getCancelledAt() != null);
    }

    @Test
    void cancelBooking_whenGuide_cancelsSuccessfully() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(10L, 200L, false);

        verify(bookingRepository).save(booking);
        assert (booking.getStatus() == Booking.BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_whenAdmin_cancelsSuccessfully() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(10L, 1L, true);

        verify(bookingRepository).save(booking);
        assert (booking.getStatus() == Booking.BookingStatus.CANCELLED);
    }
}
