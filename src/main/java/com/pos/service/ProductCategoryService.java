package com.pos.service;

import com.pos.entity.Product;
import com.pos.entity.ProductCategory;
import com.pos.entity.Shop;
import com.pos.repository.ProductCategoryRepository;
import com.pos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCategoryService {
    public static final String OTHER_CATEGORY = "အခြား";

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public ProductCategoryService(
            ProductCategoryRepository categoryRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public List<ProductCategory> list(String username) {
        Shop shop = currentUserService.require(username).getShop();
        ensureDefaults(shop);
        return categoryRepository.findByShopIdOrderBySystemCategoryDescNameAsc(shop.getId());
    }

    @Transactional
    public ProductCategory create(String name, String username) {
        Shop shop = currentUserService.require(username).getShop();
        return findOrCreate(shop, name, false);
    }

    @Transactional
    public ProductCategory update(Long id, String name, String username) {
        ProductCategory category = categoryRepository.findByIdAndShopId(id, shopId(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        String normalized = normalizeName(name);
        if (category.getName().equals(normalized)) return category;
        categoryRepository.findByShopIdAndNameIgnoreCase(category.getShop().getId(), normalized)
                .filter(existing -> !existing.getId().equals(category.getId()))
                .ifPresent(existing -> { throw new IllegalArgumentException("ဒီအမျိုးအစား ရှိပြီးသားဖြစ်ပါသည်"); });

        String previousName = category.getName();
        category.setName(normalized);
        productRepository.findByShopIdAndDeletedFalse(category.getShop().getId()).stream()
                .filter(product -> previousName.equals(product.getCategory()))
                .forEach(product -> product.setCategory(normalized));
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id, String username) {
        ProductCategory category = categoryRepository.findByIdAndShopId(id, shopId(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (Boolean.TRUE.equals(category.getSystemCategory())) {
            throw new IllegalArgumentException("အခြား category ကို ဖျက်လို့မရပါ");
        }
        List<Product> products = productRepository.findByShopIdAndDeletedFalse(category.getShop().getId());
        products.stream()
                .filter(product -> category.getName().equals(product.getCategory()))
                .forEach(product -> product.setCategory(OTHER_CATEGORY));
        productRepository.saveAll(products);
        categoryRepository.delete(category);
    }

    @Transactional
    public void ensureForShop(Shop shop, String categoryName) {
        findOrCreate(shop, categoryName, OTHER_CATEGORY.equals(normalizeName(categoryName)));
    }

    private ProductCategory findOrCreate(Shop shop, String name, boolean systemCategory) {
        String normalized = normalizeName(name);
        return categoryRepository.findByShopIdAndNameIgnoreCase(shop.getId(), normalized)
                .orElseGet(() -> {
                    ProductCategory category = new ProductCategory();
                    category.setShop(shop);
                    category.setName(normalized);
                    category.setSystemCategory(systemCategory);
                    return categoryRepository.save(category);
                });
    }

    private void ensureDefaults(Shop shop) {
        findOrCreate(shop, OTHER_CATEGORY, true);
    }

    private Long shopId(String username) {
        return currentUserService.require(username).getShop().getId();
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return OTHER_CATEGORY;
        String normalized = name.trim();
        if (normalized.length() > 100) throw new IllegalArgumentException("Category name is too long");
        return normalized;
    }
}
