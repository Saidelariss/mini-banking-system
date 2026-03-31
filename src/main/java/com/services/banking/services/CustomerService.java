package com.services.banking.services;

import com.services.banking.dtos.base.Customer;
import com.services.banking.dtos.request.CreateCustomerRequest;

import java.util.List;

public interface CustomerService {
    List<Customer> getAllCustomers();
    Customer getCustomerById(Integer id);
    Customer createCustomer(CreateCustomerRequest request);
}
