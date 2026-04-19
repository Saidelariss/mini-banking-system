package com.services.banking.services;

import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.request.TransactionRequest;
import com.services.banking.dtos.response.AccountResponse;
import com.services.banking.dtos.response.TransactionResponse;
import com.services.banking.enums.AccountStatus;
import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import com.services.banking.persistence.entities.AccountEntity;
import com.services.banking.persistence.entities.CustomerEntity;
import com.services.banking.persistence.entities.TransactionEntity;
import com.services.banking.persistence.repositories.AccountJpaRepository;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.persistence.repositories.TransactionJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final ModelMapper modelMapper;
    private final AccountJpaRepository accountJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;

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
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountJpaRepository.findAll(pageable)
                .map(accountEntity -> modelMapper.map(accountEntity, AccountResponse.class));

    }

    @Override
    public AccountResponse getAccountById(Integer accountId) {
        AccountEntity accountEntity = accountJpaRepository.findById(accountId).orElseThrow(() -> new FunctionalError("account with id " + accountId + " not found"));
        return modelMapper.map(accountEntity, AccountResponse.class);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(Integer customerId) {
        CustomerEntity customerEntity = customerJpaRepository.findById(customerId)
                .orElseThrow(() -> new FunctionalError("customer with id " + customerId + " not found"));
        return customerEntity.getAccounts().stream().map(accountEntity -> modelMapper.map(accountEntity, AccountResponse.class)).toList();
    }

    @Override
    @Transactional
    public TransactionResponse deposit(Integer accountId, TransactionRequest request) {
        AccountEntity accountEntity = accountJpaRepository.findById(accountId).orElseThrow(() -> new FunctionalError("account with id " + accountId + " not found"));
        accountEntity.setBalance(accountEntity.getBalance().add(request.getAmount()));
        accountJpaRepository.save(accountEntity);
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setReference(UUID.randomUUID().toString());
        transactionEntity.setAmount(request.getAmount());
        transactionEntity.setDescription(request.getDescription());
        transactionEntity.setDestinationAccount(accountEntity);
        transactionEntity.setStatus(TransactionStatus.SUCCESS);
        transactionEntity.setType(TransactionType.DEPOSIT);
        TransactionEntity savedTransaction = transactionJpaRepository.save(transactionEntity);
        return modelMapper.map(savedTransaction, TransactionResponse.class);

    }

    @Override
    public TransactionResponse withdraw(Integer accountId, TransactionRequest request) {
        AccountEntity accountEntity = accountJpaRepository.findById(accountId).orElseThrow(() -> new FunctionalError("account with id " + accountId + " not found"));
        BigDecimal balance = accountEntity.getBalance();
        if (balance.compareTo(request.getAmount()) < 0) {
            throw new FunctionalError("balance insufficient");
        }
        accountEntity.setBalance(balance.subtract(request.getAmount()));
        accountJpaRepository.save(accountEntity);
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setReference(UUID.randomUUID().toString());
        transactionEntity.setAmount(request.getAmount());
        transactionEntity.setDescription(request.getDescription());
        transactionEntity.setDestinationAccount(accountEntity);
        transactionEntity.setStatus(TransactionStatus.SUCCESS);
        transactionEntity.setType(TransactionType.WITHDRAW);
        TransactionEntity savedTransaction = transactionJpaRepository.save(transactionEntity);
        return modelMapper.map(savedTransaction, TransactionResponse.class);
    }
}
