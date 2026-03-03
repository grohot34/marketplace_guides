package com.example.backend.repository;

import com.example.backend.model.Booking;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "tour", "guide"})
    List<Booking> findByCustomer(User customer);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "tour", "guide"})
    List<Booking> findByGuide(User guide);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "tour", "guide"})
    List<Booking> findByTourId(Long tourId);
    
    @Query("SELECT b FROM Booking b WHERE b.guide = :guide AND b.status = :status")
    List<Booking> findByGuideAndStatus(@Param("guide") User guide, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.customer = :customer AND b.status = :status")
    List<Booking> findByCustomerAndStatus(@Param("customer") User customer, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.tourDateTime BETWEEN :start AND :end")
    List<Booking> findBookingsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tour = :tour AND b.tourDateTime = :tourDateTime AND b.status IN ('CONFIRMED', 'IN_PROGRESS')")
    Long countActiveBookingsForTourAndDateTime(@Param("tour") com.example.backend.model.Tour tour, @Param("tourDateTime") LocalDateTime tourDateTime);
}
