package com.pos.service;

import com.pos.dto.response.SubscriptionPaymentResponse;
import com.pos.entity.Shop;
import com.pos.entity.SubscriptionPayment;
import com.pos.enums.SubscriptionPaymentStatus;
import com.pos.repository.ShopRepository;
import com.pos.repository.SubscriptionPaymentRepository;
import com.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SubscriptionPaymentService {
    private final SubscriptionPaymentRepository paymentRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final Path uploadRoot;

    public SubscriptionPaymentService(
            SubscriptionPaymentRepository paymentRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            SubscriptionService subscriptionService,
            @Value("${app.upload-dir:uploads}") String uploadDir
    ) {
        this.paymentRepository = paymentRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public List<SubscriptionPaymentResponse> recent() {
        return paymentRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(SubscriptionPaymentResponse::from)
                .toList();
    }

    public List<SubscriptionPaymentResponse> forShop(Long shopId) {
        return paymentRepository.findByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(SubscriptionPaymentResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionPaymentResponse submitProof(
            String username,
            Integer months,
            BigDecimal amount,
            String paymentMethod,
            String notes,
            MultipartFile screenshot
    ) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getShop() == null) {
            throw new IllegalArgumentException("Shop account is missing");
        }
        if (screenshot == null || screenshot.isEmpty()) {
            throw new IllegalArgumentException("Payment screenshot is required");
        }
        return create(
                user.getShop().getId(),
                SubscriptionPaymentStatus.UNPAID,
                months,
                amount,
                paymentMethod,
                notes,
                false,
                screenshot
        );
    }

    @Transactional
    public SubscriptionPaymentResponse confirm(Long paymentId) {
        SubscriptionPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found"));
        payment.setStatus(SubscriptionPaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        SubscriptionPayment saved = paymentRepository.save(payment);
        subscriptionService.extendMonths(saved.getShop(), saved.getMonths());
        return SubscriptionPaymentResponse.from(saved);
    }

    @Transactional
    public SubscriptionPaymentResponse create(
            Long shopId,
            SubscriptionPaymentStatus status,
            Integer months,
            BigDecimal amount,
            String paymentMethod,
            String notes,
            Boolean extendSubscription,
            MultipartFile screenshot
    ) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));
        int safeMonths = months == null || months <= 0 ? 1 : months;

        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setShop(shop);
        payment.setStatus(status == null ? SubscriptionPaymentStatus.UNPAID : status);
        payment.setMonths(safeMonths);
        payment.setAmount(amount == null ? BigDecimal.ZERO : amount);
        payment.setPaymentMethod(blankToNull(paymentMethod));
        payment.setNotes(blankToNull(notes));

        if (payment.getStatus() == SubscriptionPaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
        }

        if (screenshot != null && !screenshot.isEmpty()) {
            String screenshotUrl = saveScreenshot(shopId, screenshot);
            payment.setScreenshotUrl(screenshotUrl);
            payment.setScreenshotPath(uploadRoot.resolve(screenshotUrl.replaceFirst("^/uploads/", "")).toString());
        }

        SubscriptionPayment saved = paymentRepository.save(payment);
        if (payment.getStatus() == SubscriptionPaymentStatus.PAID && Boolean.TRUE.equals(extendSubscription)) {
            subscriptionService.extendMonths(shop, safeMonths);
        }
        return SubscriptionPaymentResponse.from(saved);
    }

    private String saveScreenshot(Long shopId, MultipartFile file) {
        try {
            String original = file.getOriginalFilename() == null ? "payment" : file.getOriginalFilename();
            String extension = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", "");
            }
            if (extension.isBlank()) extension = ".jpg";

            Path directory = uploadRoot.resolve("payment-screenshots");
            Files.createDirectories(directory);
            String fileName = "shop-" + shopId + "-" + UUID.randomUUID() + extension;
            Path destination = directory.resolve(fileName).normalize();
            if (!destination.startsWith(directory)) {
                throw new IllegalArgumentException("Invalid file path");
            }
            file.transferTo(destination);
            return "/uploads/payment-screenshots/" + fileName;
        } catch (Exception error) {
            throw new RuntimeException("Payment screenshot upload failed", error);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
