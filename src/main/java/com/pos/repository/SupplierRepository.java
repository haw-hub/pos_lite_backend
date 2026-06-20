package com.pos.repository;

import com.pos.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByShopIdOrderByNameAsc(Long shopId);
    Optional<Supplier> findByShopIdAndNameIgnoreCase(Long shopId, String name);
}
