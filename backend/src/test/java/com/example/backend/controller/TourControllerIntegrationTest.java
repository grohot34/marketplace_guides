package com.example.backend.controller;

import com.example.backend.BackendApplication;
import com.example.backend.BaseIntegrationTest;
import com.example.backend.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tour and Auth API integration tests")
class TourControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @org.springframework.boot.test.mock.mockito.MockBean
    private KafkaTemplate<?, ?> kafkaTemplate;

    @Test
    void getTours_returnsOk() throws Exception {
        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getToursTopRated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/tours/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCategories_returnsOk() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void register_withValidBody_returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser" + System.currentTimeMillis());
        request.setEmail("test" + System.currentTimeMillis() + "@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(request.getUsername()));
    }

    @Test
    void register_withDuplicateUsername_returnsError() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicateuser");
        request.setEmail("dup@example.com");
        request.setPassword("password123");
        request.setFirstName("Dup");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }
}
