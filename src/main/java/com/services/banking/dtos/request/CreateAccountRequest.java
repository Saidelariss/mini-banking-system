package com.services.banking.dtos.request;

import com.services.banking.enums.AccountType;
import com.services.banking.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotNull(message = "customerId is required")
    private Integer customerId;
    @NotNull(message = "accountNumber is required")
    private String accountNumber;
    @NotNull(message = "accountType is required")
    private AccountType accountType;
    @NotNull(message = "currency is required")
    private CurrencyCode currency;
    private BigDecimal initialBalance;
}
