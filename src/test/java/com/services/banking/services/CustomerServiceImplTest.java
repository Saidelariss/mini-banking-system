package com.services.banking.services;

import com.services.banking.dtos.base.Customer;
import com.services.banking.dtos.request.CreateCustomerRequest;
import com.services.banking.persistence.entities.CustomerEntity;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerJpaRepository customerJpaRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerJpaRepository, new ModelMapper());
    }

    @Test
    void getAllCustomersShouldReturnMappedCustomers() {
        when(customerJpaRepository.findAll()).thenReturn(List.of(
                buildCustomerEntity(1, "Said", "Ait"),
                buildCustomerEntity(2, "Lina", "Bennani")
        ));

        List<Customer> results = customerService.getAllCustomers();

        assertAll(
                () -> assertEquals(2, results.size()),
                () -> assertEquals("Said", results.get(0).getFirstName()),
                () -> assertEquals("Lina", results.get(1).getFirstName())
        );
    }

    @Test
    void getCustomerByIdShouldReturnMappedCustomer() {
        when(customerJpaRepository.findById(5)).thenReturn(Optional.of(buildCustomerEntity(5, "Nora", "Idrissi")));

        Customer result = customerService.getCustomerById(5);

        assertAll(
                () -> assertEquals(5, result.getId()),
                () -> assertEquals("Nora", result.getFirstName()),
                () -> assertEquals("Idrissi", result.getLastName())
        );
    }

    @Test
    void getCustomerByIdShouldThrowWhenCustomerDoesNotExist() {
        when(customerJpaRepository.findById(404)).thenReturn(Optional.empty());

        FunctionalError error = assertThrows(
                FunctionalError.class,
                () -> customerService.getCustomerById(404)
        );

        assertEquals("customer not found with id : 404", error.getMessage());
    }

    @Test
    void createCustomerShouldSaveAndReturnMappedCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Amine");
        request.setLastName("El Fassi");
        request.setEmail("amine@example.com");
        request.setPhone("0600000000");

        when(customerJpaRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity entity = invocation.getArgument(0);
            entity.setId(9);
            return entity;
        });

        Customer result = customerService.createCustomer(request);

        assertAll(
                () -> assertEquals(9, result.getId()),
                () -> assertEquals("Amine", result.getFirstName()),
                () -> assertEquals("El Fassi", result.getLastName()),
                () -> assertEquals("amine@example.com", result.getEmail())
        );
        verify(customerJpaRepository).save(any(CustomerEntity.class));
    }

    private static CustomerEntity buildCustomerEntity(Integer id, String firstName, String lastName) {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(firstName.toLowerCase() + "@example.com");
        customer.setPhone("0600000000");
        return customer;
    }
}
