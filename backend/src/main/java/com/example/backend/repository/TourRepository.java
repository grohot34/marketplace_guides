package com.example.backend.repository;

import com.example.backend.model.Tour;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category", "guide"})
    List<Tour> findByActiveTrue();

    List<Tour> findByGuide(User guide);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category", "guide"})
    List<Tour> findByGuideAndActiveTrue(User guide);

    List<Tour> findByCategoryId(Long categoryId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category", "guide"})
    List<Tour> findByCategoryIdAndActiveTrue(Long categoryId);

    long countByCategoryIdAndActiveTrue(Long categoryId);

    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.guide " +
           "WHERE t.active = true AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Tour> searchActiveTours(@Param("query") String query);

    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.guide WHERE t.active = true ORDER BY t.averageRating DESC")
    List<Tour> findTopRatedTours();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category", "guide"})
    Optional<Tour> findByIdAndActiveTrue(Long id);
}
