package com.services.banking.services.exceptions;

public class FunctionalError extends RuntimeException {
    public FunctionalError(String message) {
        super(message);
    }
}
