// src/main/java/com/pos/config/DataInitializer.java
package com.pos.config;

import com.pos.entity.Product;
import com.pos.entity.User;
import com.pos.enums.UserRole;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("အက်မင်");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            System.out.println("Default admin created: username=admin, password=admin123");
        }

        // Create sample products in Myanmar language
        if (productRepository.count() == 0) {
            Product product1 = new Product();
            product1.setName("ဆန် (အိတ်)");
            product1.setDescription("စပါးဆန်အကြမ်း ၅၀ ကီလို");
            product1.setPrice(new BigDecimal("45000"));
            product1.setStock(100);
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("ကြက်ဥ (ဒါဇင်)");
            product2.setDescription("ကြက်အိမ်မွေး ဥများ");
            product2.setPrice(new BigDecimal("3500"));
            product2.setStock(200);
            productRepository.save(product2);

            Product product3 = new Product();
            product3.setName("သကြား ကီလို");
            product3.setDescription("အဖြူသကြား အမှုန့်");
            product3.setPrice(new BigDecimal("2500"));
            product3.setStock(150);
            productRepository.save(product3);

            System.out.println("Sample products created!");
        }
    }
}