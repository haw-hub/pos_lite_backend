package com.pos.repository;

import com.pos.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByShopIdOrderBySystemCategoryDescNameAsc(Long shopId);
    Optional<ProductCategory> findByIdAndShopId(Long id, Long shopId);
    Optional<ProductCategory> findByShopIdAndNameIgnoreCase(Long shopId, String name);
}
