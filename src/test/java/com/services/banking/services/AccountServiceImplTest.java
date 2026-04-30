package com.services.banking.services;

import com.services.banking.dtos.base.AccountSearchFilter;
import com.services.banking.dtos.request.AmountRequest;
import com.services.banking.dtos.request.CreateAccountRequest;
import com.services.banking.dtos.response.AccountResponse;
import com.services.banking.dtos.response.TransactionResponse;
import com.services.banking.enums.AccountStatus;
import com.services.banking.enums.AccountType;
import com.services.banking.enums.CurrencyCode;
import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import com.services.banking.persistence.entities.AccountEntity;
import com.services.banking.persistence.entities.CustomerEntity;
import com.services.banking.persistence.entities.TransactionEntity;
import com.services.banking.persistence.repositories.AccountJpaRepository;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.persistence.repositories.TransactionJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountJpaRepository accountJpaRepository;

    @Mock
    private CustomerJpaRepository customerJpaRepository;

    @Mock
    private TransactionJpaRepository transactionJpaRepository;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
                new ModelMapper(),
                accountJpaRepository,
                customerJpaRepository,
                transactionJpaRepository
        );
    }

    @Test
    void createAccountShouldPersistAccountForExistingCustomer() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(7);
        request.setAccountNumber("ACC-001");
        request.setAccountType(AccountType.CURRENT);
        request.setCurrency(CurrencyCode.MAD);
        request.setInitialBalance(new BigDecimal("1500.00"));

        CustomerEntity customer = new CustomerEntity();
        customer.setId(7);

        when(customerJpaRepository.findById(7)).thenReturn(Optional.of(customer));
        when(accountJpaRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity entity = invocation.getArgument(0);
            entity.setId(42);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        AccountResponse response = accountService.createAccount(request);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountJpaRepository).save(captor.capture());
        AccountEntity savedEntity = captor.getValue();

        assertAll(
                () -> assertEquals(42, response.getId()),
                () -> assertEquals("ACC-001", response.getAccountNumber()),
                () -> assertEquals(AccountStatus.ACTIVE, response.getStatus()),
                () -> assertEquals(CurrencyCode.MAD, response.getCurrency()),
                () -> assertSame(customer, savedEntity.getCustomer()),
                () -> assertEquals(AccountStatus.ACTIVE, savedEntity.getStatus()),
                () -> assertEquals("ACC-001", savedEntity.getAccountNumber())
        );
    }

    @Test
    void createAccountShouldThrowWhenCustomerDoesNotExist() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(99);

        when(customerJpaRepository.findById(99)).thenReturn(Optional.empty());

        FunctionalError error = assertThrows(
                FunctionalError.class,
                () -> accountService.createAccount(request)
        );

        assertEquals("customer with id 99 not found", error.getMessage());
        verify(accountJpaRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    void getAllAccountsShouldReturnMappedPage() {
        AccountEntity account = buildAccountEntity(1, "ACC-100", new BigDecimal("320.00"));
        PageRequest pageable = PageRequest.of(0, 10);

        when(accountJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(account), pageable, 1));

        Page<AccountResponse> result = accountService.getAllAccounts(pageable);

        assertAll(
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals("ACC-100", result.getContent().get(0).getAccountNumber()),
                () -> assertEquals(AccountStatus.ACTIVE, result.getContent().get(0).getStatus())
        );
    }

    @Test
    void getAccountByIdShouldReturnMappedResponse() {
        AccountEntity account = buildAccountEntity(8, "ACC-008", new BigDecimal("850.00"));

        when(accountJpaRepository.findById(8)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccountById(8);

        assertAll(
                () -> assertEquals(8, response.getId()),
                () -> assertEquals("ACC-008", response.getAccountNumber()),
                () -> assertEquals(new BigDecimal("850.00"), response.getBalance())
        );
    }

    @Test
    void getAccountByIdShouldThrowWhenAccountDoesNotExist() {
        when(accountJpaRepository.findById(404)).thenReturn(Optional.empty());

        FunctionalError error = assertThrows(
                FunctionalError.class,
                () -> accountService.getAccountById(404)
        );

        assertEquals("account with id 404 not found", error.getMessage());
    }

    @Test
    void getAccountsByCustomerIdShouldReturnMappedAccounts() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(11);
        customer.setAccounts(List.of(
                buildAccountEntity(1, "ACC-001", new BigDecimal("100.00")),
                buildAccountEntity(2, "ACC-002", new BigDecimal("200.00"))
        ));

        when(customerJpaRepository.findById(11)).thenReturn(Optional.of(customer));

        List<AccountResponse> results = accountService.getAccountsByCustomerId(11);

        assertAll(
                () -> assertEquals(2, results.size()),
                () -> assertEquals("ACC-001", results.get(0).getAccountNumber()),
                () -> assertEquals("ACC-002", results.get(1).getAccountNumber())
        );
    }

    @Test
    void depositShouldIncreaseBalanceAndSaveTransaction() {
        AccountEntity account = buildAccountEntity(4, "ACC-DEP", new BigDecimal("100.00"));
        AmountRequest request = AmountRequest.builder()
                .amount(new BigDecimal("25.00"))
                .description("cash in")
                .build();

        when(accountJpaRepository.findById(4)).thenReturn(Optional.of(account));
        when(transactionJpaRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity entity = invocation.getArgument(0);
            entity.setId(20);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        TransactionResponse response = accountService.deposit(4, request);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionJpaRepository).save(captor.capture());
        TransactionEntity savedTransaction = captor.getValue();

        assertAll(
                () -> assertEquals(new BigDecimal("125.00"), account.getBalance()),
                () -> assertEquals(20, response.getId()),
                () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus()),
                () -> assertEquals(TransactionType.DEPOSIT, response.getType()),
                () -> assertEquals(new BigDecimal("25.00"), response.getAmount()),
                () -> assertSame(account, savedTransaction.getDestinationAccount()),
                () -> assertEquals(TransactionType.DEPOSIT, savedTransaction.getType())
        );
    }

    @Test
    void withdrawShouldDecreaseBalanceAndSaveTransaction() {
        AccountEntity account = buildAccountEntity(5, "ACC-WITH", new BigDecimal("300.00"));
        AmountRequest request = AmountRequest.builder()
                .amount(new BigDecimal("75.00"))
                .description("cash out")
                .build();

        when(accountJpaRepository.findById(5)).thenReturn(Optional.of(account));
        when(transactionJpaRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity entity = invocation.getArgument(0);
            entity.setId(30);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        TransactionResponse response = accountService.withdraw(5, request);

        assertAll(
                () -> assertEquals(new BigDecimal("225.00"), account.getBalance()),
                () -> assertEquals(TransactionType.WITHDRAW, response.getType()),
                () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus()),
                () -> assertEquals(new BigDecimal("75.00"), response.getAmount())
        );
    }

    @Test
    void withdrawShouldThrowWhenBalanceIsInsufficient() {
        AccountEntity account = buildAccountEntity(6, "ACC-LOW", new BigDecimal("40.00"));
        AmountRequest request = AmountRequest.builder()
                .amount(new BigDecimal("50.00"))
                .description("cash out")
                .build();

        when(accountJpaRepository.findById(6)).thenReturn(Optional.of(account));

        FunctionalError error = assertThrows(
                FunctionalError.class,
                () -> accountService.withdraw(6, request)
        );

        assertAll(
                () -> assertEquals("balance insufficient", error.getMessage()),
                () -> assertEquals(new BigDecimal("40.00"), account.getBalance())
        );
        verify(transactionJpaRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void getAccountsByCriteriaShouldMapRepositoryResults() {
        AccountSearchFilter filter = new AccountSearchFilter();
        filter.setAccountNumber("ACC-777");
        filter.setStatus(AccountStatus.ACTIVE);

        AccountEntity account = buildAccountEntity(7, "ACC-777", new BigDecimal("700.00"));

        when(accountJpaRepository.findAll(anySpecification())).thenReturn(List.of(account));

        List<AccountResponse> results = accountService.getAccountsByCriteria(filter);

        assertAll(
                () -> assertEquals(1, results.size()),
                () -> assertEquals("ACC-777", results.get(0).getAccountNumber()),
                () -> assertEquals(new BigDecimal("700.00"), results.get(0).getBalance())
        );
    }

    private static AccountEntity buildAccountEntity(Integer id, String accountNumber, BigDecimal balance) {
        AccountEntity account = new AccountEntity();
        account.setId(id);
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.CURRENT);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(balance);
        account.setCurrency(CurrencyCode.MAD);

        CustomerEntity customer = new CustomerEntity();
        customer.setId(99);
        account.setCustomer(customer);

        return account;
    }

    @SuppressWarnings("unchecked")
    private static Specification<AccountEntity> anySpecification() {
        return any(Specification.class);
    }
}
