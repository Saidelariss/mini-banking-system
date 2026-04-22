package com.services.banking.services;

import com.services.banking.dtos.request.AmountRequest;
import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransferResponse;
import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import com.services.banking.persistence.entities.AccountEntity;
import com.services.banking.persistence.entities.TransactionEntity;
import com.services.banking.persistence.repositories.AccountJpaRepository;
import com.services.banking.persistence.repositories.TransactionJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransferServiceImpl implements TransferService {
    private AccountService accountService;
    private AccountJpaRepository accountJpaRepository;
    private TransactionJpaRepository transactionJpaRepository;
    private final ModelMapper modelMapper;

    @Override
    public TransferResponse doTransfer(TransferRequest request) {
        AmountRequest amountRequest = buildAmountRequest(request);

        AccountEntity sourceAccount = getAccountEntity(request.getSourceAccountId());
        AccountEntity destinationAccount = getAccountEntity(request.getDestinationAccountId());
        accountService.deposit(request.getDestinationAccountId(), amountRequest);
        accountService.withdraw(request.getSourceAccountId(), amountRequest);

        TransactionEntity savedTransactionEntity = saveTransaction(amountRequest, sourceAccount, destinationAccount);
        return buildTransferResponse(savedTransactionEntity);


    }

    @Override
    public List<TransferResponse> getAllTransfers() {
        return transactionJpaRepository.findAll()
                .stream()
                .map(transactionEntity -> modelMapper.map(transactionEntity, TransferResponse.class)).toList();

    }

    private static AmountRequest buildAmountRequest(TransferRequest request) {
        return AmountRequest.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();
    }

    private static TransferResponse buildTransferResponse(TransactionEntity savedTransactionEntity) {
        return TransferResponse.builder()
                .id(savedTransactionEntity.getId())
                .amount(savedTransactionEntity.getAmount())
                .description(savedTransactionEntity.getDescription())
                .destinationAccountId(savedTransactionEntity.getDestinationAccount().getId())
                .sourceAccountId(savedTransactionEntity.getSourceAccount().getId())
                .createdAt(savedTransactionEntity.getCreatedAt())
                .status(savedTransactionEntity.getStatus())
                .reference(savedTransactionEntity.getReference())
                .build();
    }

    private AccountEntity getAccountEntity(Integer accountId) {
        return accountJpaRepository.findById(accountId)
                .orElseThrow(() -> new FunctionalError("account with id " + accountId + " not found"));
    }

    private TransactionEntity saveTransaction(AmountRequest request, AccountEntity sourceAccount, AccountEntity destinationAccount) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setReference(UUID.randomUUID().toString());
        transactionEntity.setAmount(request.getAmount());
        transactionEntity.setDescription(request.getDescription());
        transactionEntity.setDestinationAccount(destinationAccount);
        transactionEntity.setSourceAccount(sourceAccount);
        transactionEntity.setStatus(TransactionStatus.SUCCESS);
        transactionEntity.setType(TransactionType.TRANSFER);
        return transactionJpaRepository.save(transactionEntity);
    }

}
