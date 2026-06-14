// src/main/java/com/pos/controller/ProductController.java
package com.pos.controller;

import com.pos.entity.Product;
import com.pos.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts(username()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id, username()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query, username()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts(username()));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product, username()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product, username()));
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Product> restoreProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.restoreProduct(id, username()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id, username());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<Product>> getDeletedProducts() {
        return ResponseEntity.ok(productService.getDeletedProducts(username()));
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
