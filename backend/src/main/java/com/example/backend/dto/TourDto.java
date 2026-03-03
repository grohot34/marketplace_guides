package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourDto {
    private Long id;
    
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerPerson;
    
    @NotNull
    private Integer durationHours;
    
    @NotNull
    private Integer maxParticipants;
    
    private String location;
    private String meetingPoint;
    private String itinerary;
    private String imageUrl;
    
    @NotNull
    private Long categoryId;
    private String categoryName;
    
    @NotNull
    private Long guideId;
    private String guideName;
    private String guideBio;
    private Double guideRating;
    
    private Boolean active;
    private Double averageRating;
    private Integer totalRatings;
}
