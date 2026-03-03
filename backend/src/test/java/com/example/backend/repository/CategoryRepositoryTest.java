package com.example.backend.repository;

import com.example.backend.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@DisplayName("CategoryRepository tests (@DataJpaTest)")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_andFindById() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Description");

        Category saved = categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        Category found = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Test Category");
        assertThat(found.getDescription()).isEqualTo("Description");
    }

    @Test
    void findAll_returnsAll() {
        Category c1 = new Category();
        c1.setName("Cat1");
        Category c2 = new Category();
        c2.setName("Cat2");
        categoryRepository.saveAll(java.util.List.of(c1, c2));
        entityManager.flush();

        assertThat(categoryRepository.findAll()).hasSize(2);
    }
}
