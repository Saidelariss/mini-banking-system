package com.services.banking.dtos.response;

import com.services.banking.enums.AccountStatus;
import com.services.banking.enums.AccountType;
import com.services.banking.enums.CurrencyCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResponse {
    private Integer id;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal balance;
    private CurrencyCode currency;
    private Integer customerId;
    private LocalDateTime createdAt;
}
