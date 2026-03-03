package com.example.backend.service;

import com.example.backend.dto.CategoryDto;
import com.example.backend.model.Category;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService unit tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Culture");
        category.setDescription("Cultural tours");
    }

    @Test
    void getAllCategories_returnsListFromRepository() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Culture");
    }

    @Test
    void getAllCategories_whenEmpty_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryById_whenFound_returnsDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(tourRepository.countByCategoryIdAndActiveTrue(1L)).thenReturn(5L);

        CategoryDto result = categoryService.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Culture");
    }

    @Test
    void getCategoryById_whenNotFound_throws() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createCategory_savesAndReturnsDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Nature");
        dto.setDescription("Nature tours");
        when(categoryRepository.save(org.mockito.ArgumentMatchers.any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        CategoryDto result = categoryService.createCategory(dto);

        assertThat(result.getName()).isEqualTo("Nature");
        verify(categoryRepository).save(org.mockito.ArgumentMatchers.any(Category.class));
    }

    @Test
    void updateCategory_whenNotFound_throws() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void deleteCategory_callsRepository() {
        categoryService.deleteCategory(1L);
        verify(categoryRepository).deleteById(1L);
    }
}
