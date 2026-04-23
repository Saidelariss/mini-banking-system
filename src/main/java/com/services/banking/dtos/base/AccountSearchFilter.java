package com.services.banking.dtos.base;

import com.services.banking.enums.AccountStatus;
import com.services.banking.enums.AccountType;
import lombok.Data;

@Data
public class AccountSearchFilter {
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private String customerFirstName;
}
