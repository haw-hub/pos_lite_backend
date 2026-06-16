package com.pos.repository;

import com.pos.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopIdAndDeletedFalse(Long shopId);

    Optional<Product> findByIdAndShopIdAndDeletedFalse(Long id, Long shopId);

    List<Product> findByShopIdAndNameContainingIgnoreCaseAndDeletedFalse(Long shopId, String name);

    @Query("""
           SELECT p
           FROM Product p
           WHERE p.stock < :threshold
           AND p.shop.id = :shopId
           AND p.deleted = false
           """)
    List<Product> findLowStockProducts(int threshold, Long shopId);

    List<Product> findByShopIdAndDeletedTrue(Long shopId);

    Optional<Product> findByIdAndShopId(Long id, Long shopId);
    Optional<Product> findByShopIdAndClientReference(Long shopId, String clientReference);
    long countByShopIdAndDeletedFalse(Long shopId);

}
