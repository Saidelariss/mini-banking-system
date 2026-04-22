package com.services.banking.dtos.base;

import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchTransactions {
    private Integer accountId;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal minAmount;
}
