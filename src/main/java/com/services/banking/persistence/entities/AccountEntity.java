package com.services.banking.persistence.entities;

import com.services.banking.enums.AccountStatus;
import com.services.banking.enums.AccountType;
import com.services.banking.enums.CurrencyCode;
import com.services.banking.persistence.entities.base.AuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "accounts")
public class AccountEntity extends AuditEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(nullable = false, unique = true)
    private String accountNumber;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomerEntity customer;
}
