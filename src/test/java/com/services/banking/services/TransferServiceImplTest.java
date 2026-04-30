package com.services.banking.services;

import com.services.banking.dtos.base.TransferSearchFilter;
import com.services.banking.dtos.request.AmountRequest;
import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransferResponse;
import com.services.banking.enums.AccountStatus;
import com.services.banking.enums.AccountType;
import com.services.banking.enums.CurrencyCode;
import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import com.services.banking.persistence.entities.AccountEntity;
import com.services.banking.persistence.entities.TransactionEntity;
import com.services.banking.persistence.repositories.AccountJpaRepository;
import com.services.banking.persistence.repositories.TransactionJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountJpaRepository accountJpaRepository;

    @Mock
    private TransactionJpaRepository transactionJpaRepository;

    private TransferServiceImpl transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferServiceImpl(
                accountService,
                accountJpaRepository,
                transactionJpaRepository,
                new ModelMapper()
        );
    }

    @Test
    void doTransferShouldMoveFundsAndPersistTransferTransaction() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(1);
        request.setDestinationAccountId(2);
        request.setAmount(new BigDecimal("80.00"));
        request.setDescription("invoice payment");

        AccountEntity source = buildAccount(1, "SRC-001");
        AccountEntity destination = buildAccount(2, "DST-002");

        when(accountJpaRepository.findById(1)).thenReturn(Optional.of(source));
        when(accountJpaRepository.findById(2)).thenReturn(Optional.of(destination));
        when(transactionJpaRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity entity = invocation.getArgument(0);
            entity.setId(15);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        TransferResponse response = transferService.doTransfer(request);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionJpaRepository).save(captor.capture());
        TransactionEntity savedTransaction = captor.getValue();

        InOrder inOrder = inOrder(accountService);
        inOrder.verify(accountService).deposit(eq(2), any(AmountRequest.class));
        inOrder.verify(accountService).withdraw(eq(1), any(AmountRequest.class));

        assertAll(
                () -> assertEquals(15, response.getId()),
                () -> assertEquals(new BigDecimal("80.00"), response.getAmount()),
                () -> assertEquals(TransactionStatus.SUCCESS, response.getStatus()),
                () -> assertEquals(1, response.getSourceAccountId()),
                () -> assertEquals(2, response.getDestinationAccountId()),
                () -> assertEquals(TransactionType.TRANSFER, savedTransaction.getType()),
                () -> assertEquals("invoice payment", savedTransaction.getDescription())
        );
    }

    @Test
    void doTransferShouldThrowWhenSourceAccountDoesNotExist() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(10);
        request.setDestinationAccountId(20);
        request.setAmount(new BigDecimal("50.00"));

        when(accountJpaRepository.findById(10)).thenReturn(Optional.empty());

        FunctionalError error = assertThrows(
                FunctionalError.class,
                () -> transferService.doTransfer(request)
        );

        assertEquals("account with id 10 not found", error.getMessage());
        verify(accountService, never()).deposit(any(Integer.class), any(AmountRequest.class));
        verify(accountService, never()).withdraw(any(Integer.class), any(AmountRequest.class));
    }

    @Test
    void getAllTransfersShouldReturnMappedTransfers() {
        TransactionEntity transfer = buildTransferEntity(1, 2, new BigDecimal("120.00"));

        when(transactionJpaRepository.findAll()).thenReturn(List.of(transfer));

        List<TransferResponse> results = transferService.getAllTransfers();

        assertAll(
                () -> assertEquals(1, results.size()),
                () -> assertEquals(new BigDecimal("120.00"), results.get(0).getAmount()),
                () -> assertEquals(TransactionStatus.SUCCESS, results.get(0).getStatus())
        );
    }

    @Test
    void getAllTransfersWithFiltersShouldMapRepositoryResults() {
        TransferSearchFilter filter = new TransferSearchFilter();
        filter.setAccountId(2);
        filter.setStatus(TransactionStatus.SUCCESS);

        TransactionEntity transfer = buildTransferEntity(3, 2, new BigDecimal("200.00"));

        when(transactionJpaRepository.findAll(anySpecification())).thenReturn(List.of(transfer));

        List<TransferResponse> results = transferService.getAllTransfers(filter);

        assertAll(
                () -> assertEquals(1, results.size()),
                () -> assertEquals(new BigDecimal("200.00"), results.get(0).getAmount()),
                () -> assertEquals(TransactionStatus.SUCCESS, results.get(0).getStatus())
        );
    }

    private static AccountEntity buildAccount(Integer id, String accountNumber) {
        AccountEntity account = new AccountEntity();
        account.setId(id);
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.CURRENT);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCurrency(CurrencyCode.MAD);
        account.setBalance(new BigDecimal("500.00"));
        return account;
    }

    private static TransactionEntity buildTransferEntity(Integer sourceId, Integer destinationId, BigDecimal amount) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(77);
        transaction.setReference("TRX-001");
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setDescription("transfer");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setSourceAccount(buildAccount(sourceId, "SRC-" + sourceId));
        transaction.setDestinationAccount(buildAccount(destinationId, "DST-" + destinationId));
        return transaction;
    }

    @SuppressWarnings("unchecked")
    private static Specification<TransactionEntity> anySpecification() {
        return any(Specification.class);
    }
}
