package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.assertj.core.api.SoftAssertions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        softly = new SoftAssertions();
        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("testuser");
        userRequestDto.setPassword("password123");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setUsername("testuser");
    }

    @Test
    void authenticateValidCredential() {
        String expectedToken = "test.jwt.token";
        when(authService.authenticate(any(UserRequestDto.class))).thenReturn(expectedToken);

        ResponseEntity<String> response = authController.authenticate(userRequestDto);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody()).isEqualTo(expectedToken);
        softly.assertAll();
    }

    @Test
    void authenticateInvalidCredential() {
        when(authService.authenticate(any(UserRequestDto.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        softly.assertThatThrownBy(() -> authController.authenticate(userRequestDto))
            .isInstanceOf(RuntimeException.class);
        softly.assertAll();
    }

    @Test
    void registerNewUser() {
        when(authService.register(any(UserRequestDto.class))).thenReturn(userResponseDto);

        ResponseEntity<UserResponseDto> response = authController.register(userRequestDto);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        softly.assertThat(response.getBody()).isNotNull();
        softly.assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        softly.assertAll();
    }

    @Test
    void registerExistingUserException() {
        when(authService.register(any(UserRequestDto.class)))
            .thenThrow(new RuntimeException("User already exists"));

        softly.assertThatThrownBy(() -> authController.register(userRequestDto))
            .isInstanceOf(RuntimeException.class);
        softly.assertAll();
    }
}