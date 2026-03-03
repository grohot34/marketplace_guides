package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuideProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String bio;
    private String languages;
    private String certifications;
    private Double averageRating;
    private Integer totalRatings;
    private String avatarUrl;
    private List<TourDto> tours;
}
