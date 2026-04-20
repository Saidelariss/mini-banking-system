package com.services.banking.dtos.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private Integer sourceAccountId;
    private Integer destinationAccountId;
    private BigDecimal amount;
    private String description;
}
