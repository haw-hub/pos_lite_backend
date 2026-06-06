package com.pos.controller;

import com.pos.entity.Customer;
import com.pos.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(
            CustomerRepository customerRepository
    ) {
        this.customerRepository = customerRepository;
    }

    @PostMapping
    public Customer create(
            @RequestBody Customer customer
    ) {
        return customerRepository.save(customer);
    }

    @GetMapping
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }
}