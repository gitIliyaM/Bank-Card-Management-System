package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CardRequestDto {
    @NotBlank(message = "Номер карты не может быть пустым")
    @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен состоять из 16 цифр")
    private String cardNumber;

    @NotBlank(message = "Имя владельца не может быть пустым")
    @Size(max = 255, message = "Имя владельца должно быть не длиннее 255 символов")
    private String holderName;

    public @NotBlank(message = "Номер карты не может быть пустым") @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен состоять из 16 цифр") String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(@NotBlank(message = "Номер карты не может быть пустым") @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен состоять из 16 цифр") String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public @NotBlank(message = "Имя владельца не может быть пустым") @Size(max = 255, message = "Имя владельца должно быть не длиннее 255 символов") String getHolderName() {
        return holderName;
    }

    public void setHolderName(@NotBlank(message = "Имя владельца не может быть пустым") @Size(max = 255, message = "Имя владельца должно быть не длиннее 255 символов") String holderName) {
        this.holderName = holderName;
    }

    public @Future(message = "Дата окончания действия должна быть в будущем") LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(@Future(message = "Дата окончания действия должна быть в будущем") LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @PositiveOrZero(message = "Баланс не может быть отрицательным")
    public double getBalance() {
        return balance;
    }

    public void setBalance(@PositiveOrZero(message = "Баланс не может быть отрицательным") double balance) {
        this.balance = balance;
    }

    @Future(message = "Дата окончания действия должна быть в будущем")
    @Schema(description = "Дата окончания действия карты (ГГГГ-ММ-ДД)", example = "2025-12-31")
    private LocalDate expiryDate;

    @PositiveOrZero(message = "Баланс не может быть отрицательным")
    @Schema(description = "Начальный баланс карты", example = "1000.0")
    private double balance;
}
