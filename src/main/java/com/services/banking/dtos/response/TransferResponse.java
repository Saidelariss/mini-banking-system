package com.services.banking.dtos.response;

import com.services.banking.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferResponse {
    private Integer id;
    private String reference;
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    private Integer destinationAccountId;
    private Integer sourceAccountId;
    private LocalDateTime createdAt;
}

