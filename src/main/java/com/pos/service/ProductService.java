package com.pos.service;

import com.pos.entity.Product;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public ProductService(ProductRepository productRepository, CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    public List<Product> getAllProducts(String username) {
        return productRepository.findByShopIdAndDeletedFalse(shopId(username));
    }

    public Product getProductById(Long id, String username) {
        return productRepository.findByIdAndShopIdAndDeletedFalse(id, shopId(username))
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(Product product, String username) {
        var user = currentUserService.require(username);
        if (product.getClientReference() != null && !product.getClientReference().isBlank()) {
            Product existing = productRepository.findByShopIdAndClientReference(user.getShop().getId(), product.getClientReference()).orElse(null);
            if (existing != null) {
                return existing;
            }
        }
        product.setId(null);
        product.setOwner(user);
        product.setShop(user.getShop());
        validateCostPrice(product);
        normalizeProductOptions(product);
        product.setDeleted(false);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails, String username) {
        Product product = getProductById(id, username);
        validateCostPrice(productDetails);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setWholesalePrice(productDetails.getWholesalePrice());
        product.setVipPrice(productDetails.getVipPrice());
        product.setCostPrice(productDetails.getCostPrice());
        product.setStock(productDetails.getStock());
        product.setUnitName(productDetails.getUnitName());
        product.setPackUnitName(productDetails.getPackUnitName());
        product.setPackSize(productDetails.getPackSize());
        product.setBarcode(productDetails.getBarcode());
        product.setImageUrl(productDetails.getImageUrl());
        product.setExpiryDate(productDetails.getExpiryDate());
        normalizeProductOptions(product);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id, String username) {
        Product product = productRepository.findByIdAndShopId(id, shopId(username))
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            return;
        }
        product.setDeleted(true);

        productRepository.save(product);
    }

    public List<Product> searchProducts(String query, String username) {
        return productRepository
                .findByShopIdAndNameContainingIgnoreCaseAndDeletedFalse(shopId(username), query);
    }

    public List<Product> getLowStockProducts(String username) {
        return productRepository.findLowStockProducts(10, shopId(username));
    }

    @Transactional
    public Product restoreProduct(Long id, String username) {

        Product product = productRepository.findByIdAndShopId(id, shopId(username))
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!Boolean.TRUE.equals(product.getDeleted())) {
            return product;
        }
        product.setDeleted(false);

        return productRepository.save(product);
    }

    public List<Product> getDeletedProducts(String username) {
        return productRepository.findByShopIdAndDeletedTrue(shopId(username));
    }

    private Long shopId(String username) {
        return currentUserService.require(username).getShop().getId();
    }

    private void validateCostPrice(Product product) {
        if (product.getCostPrice() == null
                || product.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cost price must be greater than zero");
        }
    }

    private void normalizeProductOptions(Product product) {
        if (product.getUnitName() == null || product.getUnitName().isBlank()) {
            product.setUnitName("ခု");
        }
        if (product.getPackSize() == null || product.getPackSize() < 1) {
            product.setPackSize(1);
        }
        if (product.getWholesalePrice() != null && product.getWholesalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            product.setWholesalePrice(null);
        }
        if (product.getVipPrice() != null && product.getVipPrice().compareTo(BigDecimal.ZERO) <= 0) {
            product.setVipPrice(null);
        }
    }
}
