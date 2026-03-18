package com.services.banking.persistence.repositories;

import com.services.banking.persistence.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Integer> {
}
