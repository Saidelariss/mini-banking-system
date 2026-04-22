package com.services.banking.dtos.response;

import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private Integer id;
    private String reference;
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    private Integer destinationAccountId;
    private Integer sourceAccountId;
    private TransactionType type;
    private LocalDateTime createdAt;
}

