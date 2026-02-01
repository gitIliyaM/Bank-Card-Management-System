package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequestDto {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;

    @Schema(description = "Роль пользователя", example = "ROLE_USER")
    private String role = "ROLE_USER";

    public @NotBlank(message = "Имя пользователя не может быть пустым") @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Имя пользователя не может быть пустым") @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов") String username) {
        this.username = username;
    }

    public @NotBlank(message = "Пароль не может быть пустым") @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Пароль не может быть пустым") @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
