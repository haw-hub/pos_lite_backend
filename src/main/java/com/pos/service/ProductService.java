package com.pos.service;

import com.pos.entity.Product;
import com.pos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalse();
    }

    public Product getProductById(Long id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(Product product) {
        product.setDeleted(false);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setBarcode(productDetails.getBarcode());
        product.setImageUrl(productDetails.getImageUrl());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);

        product.setDeleted(true);

        productRepository.save(product);
    }

    public List<Product> searchProducts(String query) {
        return productRepository
                .findByNameContainingIgnoreCaseAndDeletedFalse(query);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts(10);
    }

    @Transactional
    public Product restoreProduct(Long id) {

        Product product = productRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new RuntimeException("Deleted product not found"));

        product.setDeleted(false);

        return productRepository.save(product);
    }

    public List<Product> getDeletedProducts() {
        return productRepository.findByDeletedTrue();
    }
}