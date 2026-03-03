package com.example.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate bcrypt password hashes for database migrations
 * Run this main method to generate hashes for passwords
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("guide123: " + encoder.encode("guide123"));
        System.out.println("customer123: " + encoder.encode("customer123"));
    }
}
