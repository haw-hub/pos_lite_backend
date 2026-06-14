package com.pos.repository;

import com.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByOwnerUsernameAndDeletedFalse(String username);

    Optional<Product> findByIdAndOwnerUsernameAndDeletedFalse(Long id, String username);

    List<Product> findByOwnerUsernameAndNameContainingIgnoreCaseAndDeletedFalse(String username, String name);

    @Query("""
           SELECT p
           FROM Product p
           WHERE p.stock < :threshold
           AND p.owner.username = :username
           AND p.deleted = false
           """)
    List<Product> findLowStockProducts(int threshold, String username);

    List<Product> findByOwnerUsernameAndDeletedTrue(String username);

    Optional<Product> findByIdAndOwnerUsername(Long id, String username);
    Optional<Product> findByOwnerUsernameAndClientReference(String username, String clientReference);

}
