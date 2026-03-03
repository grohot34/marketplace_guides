package com.example.backend.controller;

import com.example.backend.dto.TourDto;
import com.example.backend.model.Tour;
import com.example.backend.repository.TourRepository;
import com.example.backend.service.TourService;
import com.example.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
@Tag(name = "Tours", description = "Tour management endpoints")
public class TourController {

    private final TourService tourService;
    private final TourRepository tourRepository;
    private final SecurityUtil securityUtil;

    @GetMapping
    @Operation(summary = "Get all active tours")
    public ResponseEntity<List<TourDto>> getAllTours() {
        return ResponseEntity.ok(tourService.getAllTours());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tour by ID")
    public ResponseEntity<TourDto> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get tours by category")
    public ResponseEntity<List<TourDto>> getToursByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(tourService.getToursByCategory(categoryId));
    }

    @GetMapping("/guide/{guideId}")
    @Operation(summary = "Get tours by guide")
    public ResponseEntity<List<TourDto>> getToursByGuide(@PathVariable Long guideId) {
        return ResponseEntity.ok(tourService.getToursByGuide(guideId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search tours")
    public ResponseEntity<List<TourDto>> searchTours(@RequestParam String query) {
        return ResponseEntity.ok(tourService.searchTours(query));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated tours")
    public ResponseEntity<List<TourDto>> getTopRatedTours() {
        return ResponseEntity.ok(tourService.getTopRatedTours());
    }

    @GetMapping("/my-tours")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Get current guide's tours")
    public ResponseEntity<List<TourDto>> getMyTours(Authentication authentication) {
        Long guideId = securityUtil.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(tourService.getToursByGuide(guideId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Create a new tour")
    public ResponseEntity<TourDto> createTour(
            @Valid @RequestBody TourDto tourDto,
            Authentication authentication) {
        Long guideId = securityUtil.getUserIdFromAuthentication(authentication);
        if (tourDto.getGuideId() == null) {
            tourDto.setGuideId(guideId);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.createTour(tourDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Update tour")
    public ResponseEntity<TourDto> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourDto tourDto,
            Authentication authentication) {
        Long currentUserId = securityUtil.getUserIdFromAuthentication(authentication);
        Tour existingTour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found"));
        if (!existingTour.getGuide().getId().equals(currentUserId) &&
            !securityUtil.getUserRoleFromAuthentication(authentication).equals(com.example.backend.model.User.Role.ADMIN)) {
            throw new RuntimeException("Guide can only update their own tours");
        }
        return ResponseEntity.ok(tourService.updateTour(id, tourDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Delete tour")
    public ResponseEntity<Void> deleteTour(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = securityUtil.getUserIdFromAuthentication(authentication);
        Tour existingTour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found"));
        if (!existingTour.getGuide().getId().equals(currentUserId) &&
            !securityUtil.getUserRoleFromAuthentication(authentication).equals(com.example.backend.model.User.Role.ADMIN)) {
            throw new RuntimeException("Guide can only delete their own tours");
        }
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}
