package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "avatar_url")
    private String avatarUrl;
    private String bio;
    private String languages;
    private String certifications;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Tour> tours = new HashSet<>();

    public enum Role {
        CUSTOMER, GUIDE, ADMIN
    }
}

