package com.example.backend.service;

import com.example.backend.dto.TourDto;
import com.example.backend.model.Category;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourService unit tests")
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private TourService tourService;

    private Category category;
    private User guide;
    private Tour tour;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("History");

        guide = new User();
        guide.setId(2L);
        guide.setFirstName("Ivan");
        guide.setLastName("Guide");
        guide.setRole(User.Role.GUIDE);

        tour = new Tour();
        tour.setId(10L);
        tour.setTitle("City Tour");
        tour.setDescription("Walk");
        tour.setPricePerPerson(BigDecimal.valueOf(50));
        tour.setDurationHours(2);
        tour.setMaxParticipants(10);
        tour.setCategory(category);
        tour.setGuide(guide);
        tour.setActive(true);
    }

    @Test
    void getAllTours_returnsListFromRepository() {
        when(tourRepository.findByActiveTrue()).thenReturn(List.of(tour));

        List<TourDto> result = tourService.getAllTours();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("City Tour");
        assertThat(result.get(0).getPricePerPerson()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void getTourById_whenFound_returnsDto() {
        when(tourRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(tour));

        TourDto result = tourService.getTourById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("City Tour");
        assertThat(result.getGuideName()).isEqualTo("Ivan Guide");
    }

    @Test
    void getTourById_whenNotFound_throws() {
        when(tourRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.getTourById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tour not found");
    }

    @Test
    void createTour_whenCategoryNotFound_throws() {
        TourDto dto = new TourDto();
        dto.setCategoryId(1L);
        dto.setGuideId(2L);
        dto.setTitle("T");
        dto.setPricePerPerson(BigDecimal.ONE);
        dto.setDurationHours(1);
        dto.setMaxParticipants(5);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.createTour(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createTour_whenUserNotGuide_throws() {
        guide.setRole(User.Role.CUSTOMER);
        TourDto dto = new TourDto();
        dto.setCategoryId(1L);
        dto.setGuideId(2L);
        dto.setTitle("T");
        dto.setPricePerPerson(BigDecimal.ONE);
        dto.setDurationHours(1);
        dto.setMaxParticipants(5);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guide));

        assertThatThrownBy(() -> tourService.createTour(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must be a guide");
    }

    @Test
    void createTour_whenValid_savesAndReturnsDto() {
        TourDto dto = new TourDto();
        dto.setCategoryId(1L);
        dto.setGuideId(2L);
        dto.setTitle("New Tour");
        dto.setDescription("Desc");
        dto.setPricePerPerson(BigDecimal.valueOf(30));
        dto.setDurationHours(1);
        dto.setMaxParticipants(6);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guide));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> {
            Tour t = inv.getArgument(0);
            t.setId(99L);
            return t;
        });

        TourDto result = tourService.createTour(dto);

        assertThat(result.getTitle()).isEqualTo("New Tour");
        assertThat(result.getPricePerPerson()).isEqualByComparingTo(BigDecimal.valueOf(30));
        verify(tourRepository).save(any(Tour.class));
    }

    @Test
    void updateTour_whenNotFound_throws() {
        TourDto dto = new TourDto();
        dto.setTitle("Updated");
        when(tourRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.updateTour(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tour not found");
    }

    @Test
    void deleteTour_setsActiveFalse() {
        when(tourRepository.findById(10L)).thenReturn(Optional.of(tour));
        when(tourRepository.save(any(Tour.class))).thenReturn(tour);

        tourService.deleteTour(10L);

        assertThat(tour.getActive()).isFalse();
        verify(tourRepository).save(tour);
    }
}
