package com.services.banking.persistence.entities;

import com.services.banking.enums.TransactionStatus;
import com.services.banking.enums.TransactionType;
import com.services.banking.persistence.entities.base.AuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class TransactionEntity extends AuditEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(nullable = false)
    private String reference;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private AccountEntity sourceAccount;
    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private AccountEntity destinationAccount;
}
