// src/main/java/com/pos/service/AuthService.java
package com.pos.service;

import com.pos.dto.request.LoginRequest;
import com.pos.dto.request.SignupRequest;
import com.pos.dto.response.AuthResponse;
import com.pos.dto.response.SignupResponse;
import com.pos.entity.User;
import com.pos.entity.Shop;
import com.pos.enums.UserRole;
import com.pos.repository.UserRepository;
import com.pos.repository.ShopRepository;
import com.pos.util.JwtUtil;
import com.pos.util.InputSafety;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ShopRepository shopRepository;
    private final SubscriptionService subscriptionService;

    public AuthService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       ShopRepository shopRepository,
                       SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.shopRepository = shopRepository;
        this.subscriptionService = subscriptionService;
    }

    public AuthResponse login(LoginRequest request) {
        String username = InputSafety.username(request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }
        if (user.getShop() == null) {
            throw new RuntimeException("Shop account is missing");
        }
        // Load UserDetails for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().toString());
        response.setFullName(user.getFullName());
        response.setShopId(user.getShop().getId());
        response.setShopName(user.getShop().getName());
        response.setSubscriptionStatus(subscriptionService.effectiveStatus(user.getShop()));
        response.setTrialEndsAt(user.getShop().getTrialEndsAt());
        response.setSubscriptionEndsAt(user.getShop().getSubscriptionEndsAt());
        response.setEnabledFeatures(user.getShop().getEnabledFeatures());

        return response;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String username = InputSafety.username(request.getUsername());
        String fullName = InputSafety.plainText(request.getFullName(), "Full name", 100, true);
        String shopName = InputSafety.plainText(request.getShopName(), "Shop name", 255, false);
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // Check if email already exists (if email is provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        Shop shop = new Shop();
        shop.setName(shopName == null ? fullName + " Shop" : shopName);
        shop.setPhone(request.getPhone());
        subscriptionService.initializeTrial(shop);
        shop = shopRepository.save(shop);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(fullName);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.ADMIN);  // Default role for new users
        user.setShop(shop);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Build response
        SignupResponse response = new SignupResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setFullName(savedUser.getFullName());
        response.setEmail(savedUser.getEmail());
        response.setPhone(savedUser.getPhone());
        response.setRole(savedUser.getRole().toString());
        response.setShopId(shop.getId());
        response.setShopName(shop.getName());
        response.setMessage("User registered successfully");

        return response;
    }
}
