package com.services.banking.dtos.base;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Customer {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
