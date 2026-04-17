package com.services.banking.services;

import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.response.AccountResponse;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);
    List<AccountResponse> getAllAccounts();
    AccountResponse getAccountById(Integer accountId);
}
