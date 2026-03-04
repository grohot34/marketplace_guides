package com.example.backend.service;

import com.example.backend.dto.GuideProfileDto;
import com.example.backend.dto.TourDto;
import com.example.backend.model.User;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuideProfileService {

    private final UserRepository userRepository;
    private final TourService tourService;
    private final ReviewRepository reviewRepository;

    public GuideProfileDto getGuideProfile(Long guideId) {
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));
        if (guide.getRole() != User.Role.GUIDE && guide.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("User is not a guide");
        }

        List<TourDto> tourDtos = tourService.getToursByGuide(guideId);
        String fullName = (guide.getFirstName() != null ? guide.getFirstName() : "") + " "
                + (guide.getLastName() != null ? guide.getLastName() : "").trim();

        Double avgRating = reviewRepository.getAverageRatingByGuideId(guideId);
        Long totalRatings = reviewRepository.countByGuideId(guideId);
        return new GuideProfileDto(
                guide.getId(),
                guide.getFirstName(),
                guide.getLastName(),
                fullName,
                guide.getBio(),
                guide.getLanguages(),
                guide.getCertifications(),
                avgRating != null ? avgRating : 0.0,
                totalRatings != null ? totalRatings.intValue() : 0,
                guide.getAvatarUrl(),
                tourDtos
        );
    }
}
