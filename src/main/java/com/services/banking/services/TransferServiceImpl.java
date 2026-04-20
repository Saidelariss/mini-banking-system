package com.services.banking.services;

import com.services.banking.dtos.request.AmountRequest;
import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransactionResponse;
import com.services.banking.dtos.response.TransferResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TransferServiceImpl implements TransferService{
    private AccountService accountService;
    @Override
    public TransferResponse doTransfer(TransferRequest request) {
        AmountRequest amountRequest = AmountRequest.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        TransactionResponse depositResponse = accountService.deposit(request.getDestinationAccountId(), amountRequest);
        TransactionResponse withdrawResponse = accountService.withdraw(request.getSourceAccountId(), amountRequest);

        return TransferResponse.builder()
                .amount(depositResponse.getAmount())
                .description(depositResponse.getDescription())
                .destinationAccountId()
    }
}
