package com.services.banking.apis;

import com.services.banking.services.exceptions.FunctionalError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FunctionalError.class)
    public ResponseEntity<?> handle(FunctionalError ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", Instant.now(),
                        "status", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "message", ex.getMessage()
                )
        );
    }
}

