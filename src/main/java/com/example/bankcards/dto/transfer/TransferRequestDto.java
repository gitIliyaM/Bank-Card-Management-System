package com.example.bankcards.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferRequestDto {
    @NotNull(message = "ID исходной карты не может быть пустым")
    private Long sourceCardId;

    @NotNull(message = "ID карты-получателя не может быть пустым")
    private Long destinationCardId;

    @Positive(message = "Сумма перевода должна быть положительной")
    public double getAmount() {
        return amount;
    }

    public void setAmount(@Positive(message = "Сумма перевода должна быть положительной") double amount) {
        this.amount = amount;
    }

    public @NotNull(message = "ID карты-получателя не может быть пустым") Long getDestinationCardId() {
        return destinationCardId;
    }

    public void setDestinationCardId(@NotNull(message = "ID карты-получателя не может быть пустым") Long destinationCardId) {
        this.destinationCardId = destinationCardId;
    }

    public @NotNull(message = "ID исходной карты не может быть пустым") Long getSourceCardId() {
        return sourceCardId;
    }

    public void setSourceCardId(@NotNull(message = "ID исходной карты не может быть пустым") Long sourceCardId) {
        this.sourceCardId = sourceCardId;
    }

    @Positive(message = "Сумма перевода должна быть положительной")
    @Schema(description = "Сумма перевода", example = "100.0")
    private double amount;
}