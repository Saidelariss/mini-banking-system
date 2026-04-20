package com.services.banking.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AmountRequest {
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    private String description;
}
