package com.pos.service;

import com.pos.entity.Product;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> getAllProducts(String username) {
        return productRepository.findByOwnerUsernameAndDeletedFalse(username);
    }

    public Product getProductById(Long id, String username) {
        return productRepository.findByIdAndOwnerUsernameAndDeletedFalse(id, username)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(Product product, String username) {
        if (product.getClientReference() != null && !product.getClientReference().isBlank()) {
            Product existing = productRepository.findByOwnerUsernameAndClientReference(username, product.getClientReference()).orElse(null);
            if (existing != null) {
                return existing;
            }
        }
        product.setId(null);
        product.setOwner(userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found")));
        product.setDeleted(false);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails, String username) {
        Product product = getProductById(id, username);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setBarcode(productDetails.getBarcode());
        product.setImageUrl(productDetails.getImageUrl());
        product.setExpiryDate(productDetails.getExpiryDate());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id, String username) {
        Product product = productRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            return;
        }
        product.setDeleted(true);

        productRepository.save(product);
    }

    public List<Product> searchProducts(String query, String username) {
        return productRepository
                .findByOwnerUsernameAndNameContainingIgnoreCaseAndDeletedFalse(username, query);
    }

    public List<Product> getLowStockProducts(String username) {
        return productRepository.findLowStockProducts(10, username);
    }

    @Transactional
    public Product restoreProduct(Long id, String username) {

        Product product = productRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!Boolean.TRUE.equals(product.getDeleted())) {
            return product;
        }
        product.setDeleted(false);

        return productRepository.save(product);
    }

    public List<Product> getDeletedProducts(String username) {
        return productRepository.findByOwnerUsernameAndDeletedTrue(username);
    }
}
