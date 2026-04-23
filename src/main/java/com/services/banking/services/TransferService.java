package com.services.banking.services;

import com.services.banking.dtos.base.TransferSearchFilter;
import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransferResponse;

import java.util.List;

public interface TransferService {
    TransferResponse doTransfer(TransferRequest request);

    List<TransferResponse> getAllTransfers();

    List<TransferResponse> getAllTransfers(TransferSearchFilter filters);
}
