// src/main/java/com/pos/config/DataInitializer.java
package com.pos.config;

import com.pos.entity.Product;
import com.pos.entity.User;
import com.pos.entity.Shop;
import com.pos.enums.UserRole;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import com.pos.repository.CustomerRepository;
import com.pos.repository.OrderRepository;
import com.pos.repository.ShopRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;

    public DataInitializer(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, OrderRepository orderRepository, ShopRepository shopRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create default admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            Shop defaultShop = new Shop();
            defaultShop.setName("Default Shop");
            defaultShop = shopRepository.save(defaultShop);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("အက်မင်");
            admin.setRole(UserRole.ADMIN);
            admin.setShop(defaultShop);
            admin.setActive(true);
            userRepository.save(admin);

            System.out.println("Default admin created: username=admin, password=admin123");
        }

        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Default admin user is required"));

        userRepository.findAll().forEach(user -> {
            if (user.getShop() == null) {
                Shop legacyShop = new Shop();
                legacyShop.setName((user.getFullName() == null ? user.getUsername() : user.getFullName()) + " Shop");
                legacyShop.setPhone(user.getPhone());
                user.setShop(shopRepository.save(legacyShop));
                userRepository.save(user);
            }
        });

        admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Default admin user is required"));

        User finalAdmin = admin;
        productRepository.findAll().stream()
                .forEach(product -> {
                    if (product.getOwner() == null) product.setOwner(finalAdmin);
                    if (product.getShop() == null) product.setShop(product.getOwner().getShop());
                    if (product.getCostPrice() == null) product.setCostPrice(BigDecimal.ZERO);
                    productRepository.save(product);
                });
        customerRepository.findAll().forEach(customer -> {
                    if (customer.getOwner() == null) customer.setOwner(finalAdmin);
                    if (customer.getShop() == null) customer.setShop(customer.getOwner().getShop());
                    customerRepository.save(customer);
                });

        orderRepository.findAll().forEach(order -> {
            if (order.getCashier() == null) order.setCashier(finalAdmin);
            if (order.getShop() == null) order.setShop(order.getCashier().getShop());
            BigDecimal totalProfit = BigDecimal.ZERO;
            for (var item : order.getItems()) {
                BigDecimal productCost =
                        item.getProduct() == null || item.getProduct().getCostPrice() == null
                                ? BigDecimal.ZERO
                                : item.getProduct().getCostPrice();
                if (item.getUnitCost() == null
                        || (item.getUnitCost().compareTo(BigDecimal.ZERO) == 0
                            && productCost.compareTo(BigDecimal.ZERO) > 0)) {
                    item.setUnitCost(productCost);
                }
                if (item.getProfit() == null
                        || (item.getUnitCost().compareTo(BigDecimal.ZERO) == 0
                            && productCost.compareTo(BigDecimal.ZERO) == 0)) {
                    item.setProfit(
                            item.getUnitCost().compareTo(BigDecimal.ZERO) == 0
                                    ? BigDecimal.ZERO
                                    : item.getUnitPrice()
                                            .subtract(item.getUnitCost())
                                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                    );
                }
                totalProfit = totalProfit.add(item.getProfit());
            }
            order.setTotalProfit(totalProfit);
            orderRepository.save(order);
        });

        // Create sample products in Myanmar language
        if (productRepository.count() == 0) {
            Product product1 = new Product();
            product1.setName("ဆန် (အိတ်)");
            product1.setDescription("စပါးဆန်အကြမ်း ၅၀ ကီလို");
            product1.setPrice(new BigDecimal("45000"));
            product1.setCostPrice(new BigDecimal("40000"));
            product1.setStock(100);
            product1.setOwner(finalAdmin);
            product1.setShop(finalAdmin.getShop());
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("ကြက်ဥ (ဒါဇင်)");
            product2.setDescription("ကြက်အိမ်မွေး ဥများ");
            product2.setPrice(new BigDecimal("3500"));
            product2.setCostPrice(new BigDecimal("3000"));
            product2.setStock(200);
            product2.setOwner(finalAdmin);
            product2.setShop(finalAdmin.getShop());
            productRepository.save(product2);

            Product product3 = new Product();
            product3.setName("သကြား ကီလို");
            product3.setDescription("အဖြူသကြား အမှုန့်");
            product3.setPrice(new BigDecimal("2500"));
            product3.setCostPrice(new BigDecimal("2200"));
            product3.setStock(150);
            product3.setOwner(finalAdmin);
            product3.setShop(finalAdmin.getShop());
            productRepository.save(product3);

            System.out.println("Sample products created!");
        }
    }
}
