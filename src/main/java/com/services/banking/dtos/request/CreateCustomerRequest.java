package com.services.banking.dtos.request;

import lombok.Data;

@Data
public class CreateCustomerRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
