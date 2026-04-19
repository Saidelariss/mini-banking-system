package com.services.banking.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    private String description;
}
