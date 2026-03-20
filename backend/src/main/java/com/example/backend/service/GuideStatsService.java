package com.example.backend.service;

import com.example.backend.dto.GuideStatsDto;
import com.example.backend.model.Booking;
import com.example.backend.model.User;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuideStatsService {

    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public GuideStatsDto getStatsForGuide(Long guideId) {
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));

        int totalTours = tourRepository.findByGuideAndActiveTrue(guide).size();
        List<Booking> bookings = bookingRepository.findByTour_Guide(guide);

        int pending = 0, confirmed = 0, inProgress = 0, completed = 0, cancelled = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        Map<String, Long> byStatus = new HashMap<>();
        for (Booking.BookingStatus s : Booking.BookingStatus.values()) {
            byStatus.put(s.name(), 0L);
        }

        for (Booking b : bookings) {
            byStatus.merge(b.getStatus().name(), 1L, Long::sum);
            switch (b.getStatus()) {
                case PENDING -> pending++;
                case CONFIRMED -> confirmed++;
                case IN_PROGRESS -> inProgress++;
                case COMPLETED -> {
                    completed++;
                    if (b.getTotalPrice() != null) {
                        totalRevenue = totalRevenue.add(b.getTotalPrice());
                    }
                }
                case CANCELLED -> cancelled++;
            }
        }

        Long reviewCount = reviewRepository.countByGuideId(guideId);
        Double avgRating = reviewRepository.getAverageRatingByGuideId(guideId);

        return new GuideStatsDto(
                totalTours,
                bookings.size(),
                completed,
                pending,
                confirmed,
                inProgress,
                cancelled,
                reviewCount != null ? reviewCount.intValue() : 0,
                avgRating != null ? avgRating : 0.0,
                totalRevenue,
                byStatus
        );
    }
}
