package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(length = 2000)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false)
    private BigDecimal pricePerPerson;

    @NotNull
    @Column(nullable = false)
    private Integer durationHours;

    @NotNull
    @Column(nullable = false)
    private Integer maxParticipants;

    private String location;

    private String meetingPoint;

    @Column(length = 1000)
    private String itinerary;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guide_id", nullable = false)
    private User guide;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();
}
