package com.example.backend.service;

import com.example.backend.dto.UserDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encoded");
        user.setActive(true);
    }

    @Test
    void updateCurrentUser_whenValid_updatesAndReturnsDto() {
        UserDto dto = new UserDto();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPhone("+375291234567");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(user);

        UserDto result = userService.updateCurrentUser("john", dto);

        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getPhone()).isEqualTo("+375291234567");
        verify(userRepository).save(user);
    }

    @Test
    void updateCurrentUser_whenEmailTakenByOther_throws() {
        UserDto dto = new UserDto();
        dto.setEmail("other@test.com");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCurrentUser("john", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void changePassword_whenInvalidCurrent_throws() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john", "wrong", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid current password");
    }

    @Test
    void changePassword_whenValid_updatesPassword() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(user);

        userService.changePassword("john", "oldPass", "newPass");

        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);
    }
}
