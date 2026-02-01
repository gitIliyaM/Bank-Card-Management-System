package com.example.bankcards.exception;

public class CardNumberExistsException extends RuntimeException {
    public CardNumberExistsException(String message) {
        super(message);
    }
}