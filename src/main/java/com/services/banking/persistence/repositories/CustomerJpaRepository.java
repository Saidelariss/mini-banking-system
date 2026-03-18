package com.services.banking.persistence.repositories;

import com.services.banking.persistence.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Integer> {
}
