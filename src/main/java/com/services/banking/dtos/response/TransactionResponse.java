package com.services.banking.dtos.response;

import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Integer id;
    private String reference;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    private Integer destinationAccountId;
    private LocalDateTime createdAt;
}
