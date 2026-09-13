package com.pos.controller;

import com.pos.entity.Customer;
import com.pos.repository.CustomerRepository;
import com.pos.service.CurrentUserService;
import com.pos.util.InputSafety;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    public CustomerController(
            CustomerRepository customerRepository,
            CurrentUserService currentUserService
    ) {
        this.customerRepository = customerRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public Customer create(
            @RequestBody Customer customer
    ) {
        var user = currentUserService.require(username());
        customer.setName(InputSafety.plainText(customer.getName(), "Customer name", 100, true));
        customer.setPhone(InputSafety.plainText(customer.getPhone(), "Customer phone", 30, true));
        customer.setId(null);
        customer.setOwner(user);
        customer.setShop(user.getShop());
        return customerRepository.save(customer);
    }

    @GetMapping
    public List<Customer> getAll() {
        return customerRepository.findByShopId(currentUserService.require(username()).getShop().getId());
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
