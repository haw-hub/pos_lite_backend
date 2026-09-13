package com.pos.service;

import com.pos.dto.request.PurchaseRequest;
import com.pos.entity.*;
import com.pos.enums.UserRole;
import com.pos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.pos.util.InputSafety;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    public List<Purchase> list(String username) {
        return purchaseRepository.findByShopIdOrderByCreatedAtDesc(currentUserService.require(username).getShop().getId());
    }

    @Transactional
    public Purchase stockIn(PurchaseRequest request, String username) {
        User user = currentUserService.require(username);
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER) {
            throw new RuntimeException("Only admin or manager can add stock");
        }
        if (request.getProductId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Product and positive quantity are required");
        }
        if (request.getUnitCost() == null || request.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit cost must be greater than zero");
        }

        Product product = productRepository.findByIdAndShopIdAndDeletedFalse(
                request.getProductId(), user.getShop().getId()
        ).orElseThrow(() -> new RuntimeException("Product not found"));

        Supplier supplier = null;
        String supplierName = InputSafety.plainText(request.getSupplierName(), "Supplier name", 100, false);
        String supplierPhone = InputSafety.plainText(request.getSupplierPhone(), "Supplier phone", 30, false);
        if (supplierName != null) {
            supplier = supplierRepository.findByShopIdAndNameIgnoreCase(user.getShop().getId(), supplierName)
                    .orElseGet(() -> {
                        Supplier created = new Supplier();
                        created.setShop(user.getShop());
                        created.setName(supplierName);
                        created.setPhone(supplierPhone);
                        return supplierRepository.save(created);
                    });
        }

        Purchase purchase = new Purchase();
        purchase.setShop(user.getShop());
        purchase.setProduct(product);
        purchase.setSupplier(supplier);
        purchase.setCreatedBy(user);
        purchase.setQuantity(request.getQuantity());
        purchase.setUnitCost(request.getUnitCost());
        purchase.setTotalCost(request.getUnitCost().multiply(BigDecimal.valueOf(request.getQuantity())));
        purchase.setNote(InputSafety.plainText(request.getNote(), "Purchase note", 1000, false));
        if (request.getPurchaseDate() != null && !request.getPurchaseDate().isBlank()) {
            purchase.setCreatedAt(LocalDate.parse(request.getPurchaseDate()).atStartOfDay());
        }

        product.setStock(product.getStock() + request.getQuantity());
        product.setCostPrice(request.getUnitCost());
        productRepository.save(product);

        return purchaseRepository.save(purchase);
    }
}
