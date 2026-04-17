package com.services.banking.persistence.repositories;

import com.services.banking.persistence.entities.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity,Integer> {
    Page<AccountEntity> findAll(Pageable pageable);
}
