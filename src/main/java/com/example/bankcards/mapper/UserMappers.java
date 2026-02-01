package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMappers {
    public UserResponseDto mapToDto(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());

        return dto;
    }

    public User mapToEntity(UserRequestDto userRequestDto) {
        if (userRequestDto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPassword(userRequestDto.getPassword());

        if (userRequestDto.getRole() != null) {
            user.setRole(Role.valueOf(userRequestDto.getRole()));
        } else {
            user.setRole(Role.ROLE_USER);
        }

        return user;
    }
}
