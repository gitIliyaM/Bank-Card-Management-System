package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequestDto userRequestDto;
    private User user;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        softly = new SoftAssertions();
        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("testuser");
        userRequestDto.setPassword("password");
        userRequestDto.setRole("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);
    }

    @Test
    void getUserByUsernameExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testuser");

        softly.assertThat(result).isNotNull();
        softly.assertThat(result.getUsername()).isEqualTo("testuser");
        softly.assertAll();
    }

    @Test
    void getUserByUsernameNotExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        softly.assertThatThrownBy(() -> userService.getUserByUsername("testuser"))
            .isInstanceOf(UserNotFoundException.class);
        softly.assertAll();
    }

    @Test
    void createUserExceptionUserNameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        softly.assertThatThrownBy(() -> userService.createUser(userRequestDto))
            .isInstanceOf(UserAlreadyExistsException.class);
        softly.assertAll();
    }

    @Test
    void deleteUserUserExists() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
        softly.assertAll();
    }

    @Test
    void deleteUserExceptionUserNotExists() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        softly.assertThatThrownBy(() -> userService.deleteUser(1L))
            .isInstanceOf(UserNotFoundException.class);
        softly.assertAll();
    }
}