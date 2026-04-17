package com.services.banking.services;

import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.response.AccountResponse;
import com.services.banking.enums.AccountStatus;
import com.services.banking.persistence.entities.AccountEntity;
import com.services.banking.persistence.entities.CustomerEntity;
import com.services.banking.persistence.repositories.AccountJpaRepository;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final ModelMapper modelMapper;
    private final AccountJpaRepository accountJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        CustomerEntity customerEntity = customerJpaRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new FunctionalError("customer with id " + request.getCustomerId() + " not found"));
        AccountEntity accountEntity = modelMapper.map(request, AccountEntity.class);
        accountEntity.setId(null);
        accountEntity.setStatus(AccountStatus.ACTIVE);
        accountEntity.setCustomer(customerEntity);
        AccountEntity savedAccount = accountJpaRepository.save(accountEntity);
        return modelMapper.map(savedAccount, AccountResponse.class);
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        return accountJpaRepository.findAll()
                .stream()
                .map(accountEntity -> modelMapper.map(accountEntity, AccountResponse.class))
                .toList();
    }

    @Override
    public AccountResponse getAccountById(Integer accountId) {
        AccountEntity accountEntity = accountJpaRepository.findById(accountId).orElseThrow(() -> new FunctionalError("account with id " + accountId + " not found"));
        return modelMapper.map(accountEntity, AccountResponse.class);
    }
}
