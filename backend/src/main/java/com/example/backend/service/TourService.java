package com.example.backend.service;

import com.example.backend.dto.TourDto;
import com.example.backend.model.Category;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    @Cacheable(value = "tours")
    public List<TourDto> getAllTours() {
        return tourRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "tours", key = "#id")
    public TourDto getTourById(Long id) {
        Tour tour = tourRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Tour not found"));
        return convertToDto(tour);
    }

    public List<TourDto> getToursByCategory(Long categoryId) {
        return tourRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TourDto> getToursByGuide(Long guideId) {
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new RuntimeException("Guide not found"));
        return tourRepository.findByGuideAndActiveTrue(guide).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TourDto> searchTours(String query) {
        return tourRepository.searchActiveTours(query).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TourDto> getTopRatedTours() {
        return tourRepository.findTopRatedTours().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "tours", allEntries = true)
    public TourDto createTour(TourDto tourDto) {
        Category category = categoryRepository.findById(tourDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        User guide = userRepository.findById(tourDto.getGuideId())
                .orElseThrow(() -> new RuntimeException("Guide not found"));

        if (guide.getRole() != User.Role.GUIDE && guide.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("User must be a guide to create tours");
        }

        Tour tour = new Tour();
        tour.setTitle(tourDto.getTitle());
        tour.setDescription(tourDto.getDescription());
        tour.setPricePerPerson(tourDto.getPricePerPerson());
        tour.setDurationHours(tourDto.getDurationHours());
        tour.setMaxParticipants(tourDto.getMaxParticipants());
        tour.setLocation(tourDto.getLocation());
        tour.setMeetingPoint(tourDto.getMeetingPoint());
        tour.setItinerary(tourDto.getItinerary());
        tour.setImageUrl(tourDto.getImageUrl());
        tour.setCategory(category);
        tour.setGuide(guide);
        tour.setActive(true);

        tour = tourRepository.save(tour);
        return convertToDto(tour);
    }

    @Transactional
    @CacheEvict(value = "tours", allEntries = true)
    public TourDto updateTour(Long id, TourDto tourDto) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found"));

        tour.setTitle(tourDto.getTitle());
        tour.setDescription(tourDto.getDescription());
        tour.setPricePerPerson(tourDto.getPricePerPerson());
        tour.setDurationHours(tourDto.getDurationHours());
        tour.setMaxParticipants(tourDto.getMaxParticipants());
        tour.setLocation(tourDto.getLocation());
        tour.setMeetingPoint(tourDto.getMeetingPoint());
        tour.setItinerary(tourDto.getItinerary());
        tour.setImageUrl(tourDto.getImageUrl());

        if (tourDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(tourDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            tour.setCategory(category);
        }

        tour = tourRepository.save(tour);
        return convertToDto(tour);
    }

    @Transactional
    @CacheEvict(value = "tours", allEntries = true)
    public void deleteTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found"));
        tour.setActive(false);
        tourRepository.save(tour);
    }

    private TourDto convertToDto(Tour tour) {
        TourDto dto = new TourDto();
        dto.setId(tour.getId());
        dto.setTitle(tour.getTitle());
        dto.setDescription(tour.getDescription());
        dto.setPricePerPerson(tour.getPricePerPerson());
        dto.setDurationHours(tour.getDurationHours());
        dto.setMaxParticipants(tour.getMaxParticipants());
        dto.setLocation(tour.getLocation());
        dto.setMeetingPoint(tour.getMeetingPoint());
        dto.setItinerary(tour.getItinerary());
        dto.setImageUrl(tour.getImageUrl());
        dto.setCategoryId(tour.getCategory().getId());
        dto.setCategoryName(tour.getCategory().getName());
        dto.setGuideId(tour.getGuide().getId());
        dto.setGuideName(tour.getGuide().getFirstName() + " " + tour.getGuide().getLastName());
        dto.setGuideBio(tour.getGuide().getBio());
        ReviewService.ReviewStats guideStats = reviewService.getReviewStatsForGuide(tour.getGuide().getId());
        dto.setGuideRating(guideStats != null ? guideStats.getAverageRating() : 0.0);
        dto.setActive(tour.getActive());
        ReviewService.ReviewStats tourStats = reviewService.getReviewStatsForTour(tour.getId());
        dto.setAverageRating(tourStats != null ? tourStats.getAverageRating() : 0.0);
        dto.setTotalRatings(tourStats != null ? tourStats.getReviewCount() : 0);

        return dto;
    }
}
