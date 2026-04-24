package com.services.banking.persistence.entities;

import com.services.banking.persistence.entities.base.AuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "customers")
public class CustomerEntity extends AuditEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String email;
    private String phone;
    @OneToMany(mappedBy = "customer")
    private List<AccountEntity> accounts = new ArrayList<>();
}
