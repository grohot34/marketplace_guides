package com.example.backend.service;

import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.CUSTOMER);
        user.setActive(true);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        String token = jwtTokenProvider.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole().name())
                        .build()
        );

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        log.debug("Attempting login for username: {}", request.getUsername());
        

        if (!userRepository.existsByUsername(request.getUsername())) {
            log.warn("Login attempt for non-existent user: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.debug("Authentication successful for user: {}", userDetails.getUsername());
            
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> {
                        log.error("User not found in database after successful authentication: {}", userDetails.getUsername());
                        return new RuntimeException("User not found");
                    });

            if (!user.getActive()) {
                log.warn("Attempted login for inactive user: {}", user.getUsername());
                throw new RuntimeException("User account is disabled");
            }

            String token = jwtTokenProvider.generateToken(userDetails);
            log.debug("Token generated successfully for user: {}", user.getUsername());

            return new AuthResponse(token, user.getUsername(), user.getRole().name());
        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for username: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password", e);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for username: {}", request.getUsername(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during login for username: {}", request.getUsername(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AuthResponse processOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
        String googleSub = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");

        User user = userRepository.findByGoogleSub(googleSub)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> createUserFromGoogle(googleSub, email, givenName, familyName));

        if (user.getGoogleSub() == null && googleSub != null) {
            user.setGoogleSub(googleSub);
            user = userRepository.save(user);
        }

        if (!user.getActive()) {
            throw new RuntimeException("User account is disabled");
        }

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole().name())
                        .build();

        String token = jwtTokenProvider.generateToken(userDetails);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    private User createUserFromGoogle(String googleSub, String email, String givenName, String familyName) {
        String baseUsername = email != null ? email.split("@")[0] : "google_" + googleSub.substring(0, Math.min(8, googleSub.length()));
        String username = baseUsername;
        int suffix = 0;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + (++suffix);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email != null ? email : username + "@google.oauth");
        user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setFirstName(givenName != null ? givenName : "User");
        user.setLastName(familyName != null ? familyName : "");
        user.setGoogleSub(googleSub);
        user.setRole(User.Role.CUSTOMER);
        user.setActive(true);

        user = userRepository.save(user);
        log.info("Created user from Google OAuth: {}", user.getUsername());
        return user;
    }
}

