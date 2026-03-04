package com.example.backend.repository;

import com.example.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStatus(Review.ReviewStatus status);
    List<Review> findByBooking_Tour_Guide_IdAndStatus(Long guideId, Review.ReviewStatus status);
    List<Review> findByBooking_Tour_IdAndStatus(Long tourId, Review.ReviewStatus status);
    List<Review> findByBooking_Id(Long bookingId);
    Optional<Review> findByBooking_IdAndBooking_Customer_Id(Long bookingId, Long customerId);

    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.booking b WHERE b.tour.guide.id = :guideId AND r.status = 'APPROVED'")
    Double getAverageRatingByGuideId(@Param("guideId") Long guideId);
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.booking b WHERE b.tour.id = :tourId AND r.status = 'APPROVED'")
    Double getAverageRatingByTourId(@Param("tourId") Long tourId);
    @Query("SELECT COUNT(r) FROM Review r JOIN r.booking b WHERE b.tour.guide.id = :guideId AND r.status = 'APPROVED'")
    Long countByGuideId(@Param("guideId") Long guideId);
    @Query("SELECT COUNT(r) FROM Review r JOIN r.booking b WHERE b.tour.id = :tourId AND r.status = 'APPROVED'")
    Long countByTourId(@Param("tourId") Long tourId);
}

