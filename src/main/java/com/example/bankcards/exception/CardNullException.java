package com.example.bankcards.exception;

public class CardNullException extends RuntimeException {
    public CardNullException(String message) {
        super(message);
    }
}