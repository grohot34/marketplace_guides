package com.example.backend.repository;

import com.example.backend.model.Category;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@DisplayName("TourRepository tests (@DataJpaTest)")
class TourRepositoryTest {

    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByActiveTrue_returnsOnlyActive() {
        Category cat = new Category();
        cat.setName("Cat");
        entityManager.persist(cat);
        User guide = new User();
        guide.setUsername("g");
        guide.setEmail("g@e.com");
        guide.setPassword("p");
        guide.setFirstName("G");
        guide.setLastName("G");
        entityManager.persist(guide);
        entityManager.flush();

        Tour active = new Tour();
        active.setTitle("T1");
        active.setDescription("D");
        active.setPricePerPerson(BigDecimal.TEN);
        active.setDurationHours(1);
        active.setMaxParticipants(5);
        active.setCategory(cat);
        active.setGuide(guide);
        active.setActive(true);
        tourRepository.save(active);
        entityManager.flush();
        entityManager.clear();

        assertThat(tourRepository.findByActiveTrue()).hasSize(1);
        assertThat(tourRepository.findByActiveTrue().get(0).getTitle()).isEqualTo("T1");
    }
}
