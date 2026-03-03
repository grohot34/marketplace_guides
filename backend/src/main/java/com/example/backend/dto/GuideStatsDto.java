package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuideStatsDto {
    private int totalTours;
    private int totalBookings;
    private int completedBookings;
    private int pendingBookings;
    private int confirmedBookings;
    private int inProgressBookings;
    private int cancelledBookings;
    private int totalReviews;
    private double averageRating;
    private BigDecimal totalRevenue;
    private Map<String, Long> bookingsByStatus;
}
