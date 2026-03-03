package com.example.backend.controller;

import com.example.backend.dto.GuideProfileDto;
import com.example.backend.dto.GuideStatsDto;
import com.example.backend.service.GuideProfileService;
import com.example.backend.service.GuideStatsService;
import com.example.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guides")
@RequiredArgsConstructor
@Tag(name = "Guides", description = "Guide statistics and profile endpoints")
public class GuideController {

    private final GuideStatsService guideStatsService;
    private final GuideProfileService guideProfileService;
    private final SecurityUtil securityUtil;

    @GetMapping("/{id}")
    @Operation(summary = "Get guide public profile (for tourists)")
    public ResponseEntity<GuideProfileDto> getGuideProfile(@PathVariable Long id) {
        return ResponseEntity.ok(guideProfileService.getGuideProfile(id));
    }

    @GetMapping("/me/stats")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    @Operation(summary = "Get current guide's statistics")
    public ResponseEntity<GuideStatsDto> getMyStats(Authentication authentication) {
        Long guideId = securityUtil.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(guideStatsService.getStatsForGuide(guideId));
    }
}
