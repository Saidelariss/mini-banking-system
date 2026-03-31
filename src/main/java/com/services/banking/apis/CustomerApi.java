package com.services.banking.apis;

import com.services.banking.dtos.base.Customer;
import com.services.banking.dtos.request.CreateCustomerRequest;
import com.services.banking.services.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/customers")
public class CustomerApi {
    private final CustomerService customerService;

    @GetMapping
    public List<Customer> getAllCustomers(){
        return customerService.getAllCustomers();
    }

    @PostMapping
    public Customer createCustomer(@RequestBody CreateCustomerRequest request){
       return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Integer id){
        return customerService.getCustomerById(id);
    }
}
