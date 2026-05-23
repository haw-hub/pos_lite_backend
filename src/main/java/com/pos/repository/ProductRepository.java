package com.pos.repository;

import com.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByDeletedFalse();

    Optional<Product> findByIdAndDeletedFalse(Long id);

    List<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    @Query("""
           SELECT p
           FROM Product p
           WHERE p.stock < :threshold
           AND p.deleted = false
           """)
    List<Product> findLowStockProducts(int threshold);

    List<Product> findByDeletedTrue();

    Optional<Product> findByIdAndDeletedTrue(Long id);

}