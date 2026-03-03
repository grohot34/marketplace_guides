package com.example.backend.dto;

import com.example.backend.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    
    @NotNull
    private Long customerId;
    private String customerName;
    
    @NotNull
    private Long tourId;
    private String tourTitle;
    
    @NotNull
    private Long guideId;
    private String guideName;
    
    @NotNull
    private LocalDateTime tourDateTime;
    
    @NotNull
    @Min(1)
    private Integer numberOfParticipants;
    
    private String contactPhone;
    private String specialRequests;
    
    private Booking.BookingStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    /** Есть ли отзыв к этому бронированию */
    private Boolean hasReview;
    /** ID отзыва (если есть), для редактирования */
    private Long reviewId;
    /** Оплачено ли бронирование (Stripe) */
    private Boolean paid;
}
