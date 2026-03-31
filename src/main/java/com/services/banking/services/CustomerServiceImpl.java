package com.services.banking.services;

import com.services.banking.dtos.base.Customer;
import com.services.banking.dtos.request.CreateCustomerRequest;
import com.services.banking.persistence.entities.CustomerEntity;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerJpaRepository customerJpaRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Customer> getAllCustomers() {
        return customerJpaRepository.findAll()
                .stream().map(customerEntity -> modelMapper.map(customerEntity, Customer.class))
                .toList();
    }

    @Override
    public Customer getCustomerById(Integer id) {
        CustomerEntity customerEntity = customerJpaRepository.findById(id).orElseThrow(() -> new FunctionalError("customer not found with id : " + id));
        return modelMapper.map(customerEntity, Customer.class);
    }

    @Override
    public Customer createCustomer(CreateCustomerRequest request) {
        CustomerEntity customerEntity = modelMapper.map(request, CustomerEntity.class);
        CustomerEntity savedCustomerEntity = customerJpaRepository.save(customerEntity);

        return modelMapper.map(savedCustomerEntity,Customer.class);
    }
}
