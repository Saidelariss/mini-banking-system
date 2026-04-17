package com.services.banking.apis;

import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.response.AccountResponse;
import com.services.banking.services.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
    List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountId}")
    AccountResponse getAccountById(@PathVariable Integer accountId) {
        return accountService.getAccountById(accountId);
    }

    @GetMapping("/{customerId}")
    List<AccountResponse> getAccountsByCustomerId(@PathVariable Integer customerId){
        return accountService.getAccountsByCustomerId(customerId);
    }
}
