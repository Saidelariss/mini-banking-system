package com.services.banking.persistence.repositories;

import com.services.banking.persistence.entities.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountJpaRepository extends JpaRepository<AccountEntity,Integer>, JpaSpecificationExecutor<AccountEntity> {
    Page<AccountEntity> findAll(Pageable pageable);
}
