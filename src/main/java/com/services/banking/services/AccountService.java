package com.services.banking.services;

import com.services.banking.dtos.base.AccountSearchFilter;
import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.request.AmountRequest;
import com.services.banking.dtos.response.AccountResponse;
import com.services.banking.dtos.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);

    Page<AccountResponse> getAllAccounts(Pageable pageable);

    AccountResponse getAccountById(Integer accountId);

    List<AccountResponse> getAccountsByCustomerId(Integer customerId);

    TransactionResponse deposit(Integer accountId, AmountRequest request);

    TransactionResponse withdraw(Integer accountId, AmountRequest request);

    List<AccountResponse> getAccountsByCriteria(AccountSearchFilter filters);
}
