package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        softly = new SoftAssertions();
        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("testuser");
        userRequestDto.setPassword("password");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setUsername("testuser");
    }

    @Test
    void authenticateCredentialsValid() {
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        user.setId(1L);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(eq(authentication), eq(1L))).thenReturn("token");

        String result = authService.authenticate(userRequestDto);

        softly.assertThat(result).isEqualTo("token");
        verify(authenticationManager).authenticate(any());
        softly.assertAll();
    }

    @Test
    void authenticateExceptionCredentialsInvalid() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException.class);

        softly.assertThatThrownBy(() -> authService.authenticate(userRequestDto))
            .isInstanceOf(AuthenticationException.class);
        softly.assertAll();
    }

    @Test
    void registerUsernameNotExists() {
        when(userService.createUser(any())).thenReturn(userResponseDto);

        UserResponseDto result = authService.register(userRequestDto);

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userService).createUser(userRequestDto);
        softly.assertAll();
    }
}