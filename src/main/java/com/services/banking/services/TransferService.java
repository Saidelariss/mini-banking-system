package com.services.banking.services;

import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransferResponse;

public interface TransferService {
    TransferResponse doTransfer(TransferRequest request);
}
