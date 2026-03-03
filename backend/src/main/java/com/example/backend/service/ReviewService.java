package com.example.backend.service;

import com.example.backend.dto.ReviewDto;
import com.example.backend.model.Booking;
import com.example.backend.model.Review;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return convertToDto(review);
    }

    public List<ReviewDto> getReviewsByGuide(Long guideId) {
        return reviewRepository.findByGuide_IdAndStatus(guideId, Review.ReviewStatus.APPROVED).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByTour(Long tourId) {
        return reviewRepository.findByTour_IdAndStatus(tourId, Review.ReviewStatus.APPROVED).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getAllReviewsForAdmin(Review.ReviewStatus statusFilter) {
        List<Review> list = statusFilter != null
                ? reviewRepository.findByStatus(statusFilter)
                : reviewRepository.findAll();
        return list.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public ReviewStats getReviewStatsForTour(Long tourId) {
        List<Review> reviews = reviewRepository.findByTour_IdAndStatus(tourId, Review.ReviewStatus.APPROVED);
        if (reviews.isEmpty()) {
            return new ReviewStats(0.0, 0);
        }
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        return new ReviewStats(averageRating, reviews.size());
    }

    public ReviewStats getReviewStatsForGuide(Long guideId) {
        Double avgRating = reviewRepository.getAverageRatingByGuideId(guideId);
        Long count = reviewRepository.countByGuideId(guideId);
        return new ReviewStats(avgRating != null ? avgRating : 0.0, count != null ? count.intValue() : 0);
    }

    @Transactional
    public ReviewDto createReview(Long customerId, ReviewDto reviewDto) {
        Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("You can only review your own bookings");
        }

        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("You can only review completed bookings");
        }

        reviewRepository.findByBookingIdAndCustomerId(booking.getId(), customerId)
                .ifPresent(r -> {
                    throw new RuntimeException("Review already exists for this booking");
                });

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(booking.getCustomer());
        review.setGuide(booking.getGuide());
        review.setTour(booking.getTour());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setStatus(Review.ReviewStatus.PENDING);

        review = reviewRepository.save(review);

        // Update tour and guide ratings
        updateTourRating(booking.getTour().getId());
        updateGuideRating(booking.getGuide().getId());

        return convertToDto(review);
    }

    @Transactional
    private void updateTourRating(Long tourId) {
        ReviewStats stats = getReviewStatsForTour(tourId);
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour not found"));
        tour.setAverageRating(stats.getAverageRating());
        tour.setTotalRatings(stats.getReviewCount());
        tourRepository.save(tour);
    }

    @Transactional
    private void updateGuideRating(Long guideId) {
        ReviewStats stats = getReviewStatsForGuide(guideId);
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));
        guide.setAverageRating(stats.getAverageRating());
        guide.setTotalRatings(stats.getReviewCount());
        userRepository.save(guide);
    }

    @Transactional
    public ReviewDto updateReview(Long id, Long userId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!review.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own review");
        }
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review = reviewRepository.save(review);
        updateTourRating(review.getTour().getId());
        updateGuideRating(review.getGuide().getId());
        return convertToDto(review);
    }

    @Transactional
    public void deleteReview(Long id, Long userId, boolean isAdmin) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!isAdmin && !review.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own review");
        }
        Long tourId = review.getTour().getId();
        Long guideId = review.getGuide().getId();
        reviewRepository.delete(review);
        updateTourRating(tourId);
        updateGuideRating(guideId);
    }

    @Transactional
    public ReviewDto setReviewStatus(Long id, Review.ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(status);
        review = reviewRepository.save(review);
        updateTourRating(review.getTour().getId());
        updateGuideRating(review.getGuide().getId());
        return convertToDto(review);
    }

    @Transactional
    public ReviewDto setReviewResponse(Long id, String responseText, Long adminUserId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setResponse(responseText);
        review.setRespondedAt(LocalDateTime.now());
        review.setRespondedBy(adminUserId != null ? userRepository.findById(adminUserId)
                .orElse(null) : null);
        review = reviewRepository.save(review);
        return convertToDto(review);
    }

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setBookingId(review.getBooking().getId());
        dto.setGuideId(review.getGuide().getId());
        dto.setGuideName(review.getGuide().getFirstName() + " " + review.getGuide().getLastName());
        dto.setTourId(review.getTour().getId());
        dto.setTourTitle(review.getTour().getTitle());
        dto.setCustomerId(review.getCustomer().getId());
        dto.setCustomerName(review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setStatus(review.getStatus() != null ? review.getStatus().name() : null);
        dto.setResponse(review.getResponse());
        dto.setRespondedAt(review.getRespondedAt());
        if (review.getRespondedBy() != null) {
            dto.setRespondedByName(review.getRespondedBy().getFirstName() + " " + review.getRespondedBy().getLastName());
        }
        return dto;
    }

    public static class ReviewStats {
        private final Double averageRating;
        private final Integer reviewCount;

        public ReviewStats(Double averageRating, Integer reviewCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public Integer getReviewCount() {
            return reviewCount;
        }
    }
}

