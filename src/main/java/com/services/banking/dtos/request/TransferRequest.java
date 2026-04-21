package com.services.banking.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull(message = "sourceAccountId is required")
    private Integer sourceAccountId;
    @NotNull(message = "destinationAccountId is required")
    private Integer destinationAccountId;
    @NotNull(message = "amount is required")
    private BigDecimal amount;
    private String description;
}
