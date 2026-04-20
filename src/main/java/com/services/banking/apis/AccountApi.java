package com.services.banking.apis;

import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.request.AmountRequest;
import com.services.banking.dtos.response.AccountResponse;
import com.services.banking.dtos.response.TransactionResponse;
import com.services.banking.services.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
public class AccountApi {
    private final AccountService accountService;

    @PostMapping
    AccountResponse createAccount(@RequestBody @Valid CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping
    Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountService.getAllAccounts(pageable);
    }

    @GetMapping("/{accountId}")
    AccountResponse getAccountById(@PathVariable Integer accountId) {
        return accountService.getAccountById(accountId);
    }

    @GetMapping("/customer/{customerId}")
    List<AccountResponse> getAccountsByCustomerId(@PathVariable Integer customerId) {
        return accountService.getAccountsByCustomerId(customerId);
    }

    @PostMapping("/{accountId}/deposit")
    TransactionResponse deposit(@PathVariable Integer accountId, @RequestBody @Valid AmountRequest amountRequest) {
        return accountService.deposit(accountId, amountRequest);
    }

    @PostMapping("/{accountId}/withdraw")
    TransactionResponse withdraw(@PathVariable Integer accountId, @RequestBody @Valid AmountRequest amountRequest) {
        return accountService.withdraw(accountId, amountRequest);
    }
}
