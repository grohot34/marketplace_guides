package com.example.backend.service;

import com.example.backend.dto.GuideProfileDto;
import com.example.backend.dto.TourDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuideProfileService {

    private final UserRepository userRepository;
    private final TourService tourService;

    /**
     * Публичный профиль гида для просмотра туристами.
     */
    public GuideProfileDto getGuideProfile(Long guideId) {
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));
        if (guide.getRole() != User.Role.GUIDE && guide.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("User is not a guide");
        }

        List<TourDto> tourDtos = tourService.getToursByGuide(guideId);
        String fullName = (guide.getFirstName() != null ? guide.getFirstName() : "") + " "
                + (guide.getLastName() != null ? guide.getLastName() : "").trim();

        return new GuideProfileDto(
                guide.getId(),
                guide.getFirstName(),
                guide.getLastName(),
                fullName,
                guide.getBio(),
                guide.getLanguages(),
                guide.getCertifications(),
                guide.getAverageRating() != null ? guide.getAverageRating() : 0.0,
                guide.getTotalRatings() != null ? guide.getTotalRatings() : 0,
                guide.getAvatarUrl(),
                tourDtos
        );
    }
}
