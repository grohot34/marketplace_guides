package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private Long totalUsers;
    private Long totalCustomers;
    private Long totalProviders; // гиды (totalGuides)
    private Long totalTours;
    private Long totalBookings;
    private Long pendingOrders; // для обратной совместимости
    private Long completedOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue; // выручка по завершённым бронированиям (BYN)
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> usersByRole;
    // Устаревшие поля (legacy Services/Orders) — можно не заполнять
    private Long totalServices;
    private Long totalOrders;
}








