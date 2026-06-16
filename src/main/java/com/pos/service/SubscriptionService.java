package com.pos.service;

import com.pos.entity.Shop;
import com.pos.enums.SubscriptionStatus;
import com.pos.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {
    private final ShopRepository shopRepository;
    private final TrialSettingsService trialSettingsService;
    private final SubscriptionActivityService activityService;

    public SubscriptionService(
            ShopRepository shopRepository,
            TrialSettingsService trialSettingsService,
            SubscriptionActivityService activityService
    ) {
        this.shopRepository = shopRepository;
        this.trialSettingsService = trialSettingsService;
        this.activityService = activityService;
    }

    public void initializeTrial(Shop shop) {
        shop.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        shop.setTrialEndsAt(LocalDateTime.now().plusDays(trialSettingsService.getDefaultTrialDays()));
    }

    public SubscriptionStatus effectiveStatus(Shop shop) {
        if (!shop.isActive() || shop.getSubscriptionStatus() == SubscriptionStatus.SUSPENDED) {
            return SubscriptionStatus.SUSPENDED;
        }
        LocalDateTime now = LocalDateTime.now();
        if (shop.getSubscriptionEndsAt() != null && !shop.getSubscriptionEndsAt().isBefore(now)) {
            return SubscriptionStatus.ACTIVE;
        }
        if (shop.getTrialEndsAt() != null && !shop.getTrialEndsAt().isBefore(now)) {
            return SubscriptionStatus.TRIAL;
        }
        return SubscriptionStatus.EXPIRED;
    }

    public boolean canUseApp(Shop shop) {
        SubscriptionStatus status = effectiveStatus(shop);
        return status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.ACTIVE;
    }

    @Transactional
    public Shop extendMonths(Shop shop, int months) {
        if (months < 1 || months > 36) {
            throw new IllegalArgumentException("Months must be between 1 and 36");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = shop.getSubscriptionEndsAt() != null
                && shop.getSubscriptionEndsAt().isAfter(now)
                ? shop.getSubscriptionEndsAt()
                : now;
        LocalDateTime previousEndsAt = shop.getSubscriptionEndsAt();
        shop.setSubscriptionEndsAt(base.plusMonths(months));
        shop.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        shop.setActive(true);
        Shop saved = shopRepository.save(shop);
        activityService.record(saved, "SUBSCRIPTION_EXTENDED", months, months + " month subscription confirmed",
                previousEndsAt, saved.getSubscriptionEndsAt());
        return saved;
    }

    @Transactional
    public Shop setTrialDays(Shop shop, int days) {
        if (days < 0 || days > 365) {
            throw new IllegalArgumentException("Trial days must be between 0 and 365");
        }
        LocalDateTime previousEndsAt = shop.getTrialEndsAt();
        shop.setTrialEndsAt(LocalDateTime.now().plusDays(days));
        shop.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        shop.setActive(true);
        Shop saved = shopRepository.save(shop);
        activityService.record(saved, "TRIAL_GRANTED", days, days + " trial days granted",
                previousEndsAt, saved.getTrialEndsAt());
        return saved;
    }

    @Transactional
    public Shop setSuspended(Shop shop, boolean suspended) {
        shop.setActive(!suspended);
        shop.setSubscriptionStatus(suspended
                ? SubscriptionStatus.SUSPENDED
                : effectiveStatusAfterUnsuspend(shop));
        Shop saved = shopRepository.save(shop);
        activityService.record(saved, suspended ? "SHOP_SUSPENDED" : "SHOP_RESTORED", null,
                suspended ? "Shop access suspended" : "Shop access restored",
                saved.getSubscriptionEndsAt(), saved.getSubscriptionEndsAt());
        return saved;
    }

    private SubscriptionStatus effectiveStatusAfterUnsuspend(Shop shop) {
        LocalDateTime now = LocalDateTime.now();
        if (shop.getSubscriptionEndsAt() != null && !shop.getSubscriptionEndsAt().isBefore(now)) {
            return SubscriptionStatus.ACTIVE;
        }
        if (shop.getTrialEndsAt() != null && !shop.getTrialEndsAt().isBefore(now)) {
            return SubscriptionStatus.TRIAL;
        }
        return SubscriptionStatus.EXPIRED;
    }
}
