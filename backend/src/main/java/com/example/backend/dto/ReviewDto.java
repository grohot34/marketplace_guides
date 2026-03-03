package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    
    @NotNull
    private Long bookingId;
    
    private Long guideId;
    private String guideName;
    
    private Long tourId;
    private String tourTitle;
    
    private Long customerId;
    private String customerName;
    
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String comment;
    
    private LocalDateTime createdAt;
    private String status;
    private String response;
    private LocalDateTime respondedAt;
    private String respondedByName;
}
















