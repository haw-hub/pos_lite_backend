package com.pos.repository;

import com.pos.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByShopIdAndPhone(Long shopId, String phone);
    List<Customer> findByShopId(Long shopId);
}
