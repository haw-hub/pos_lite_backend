package com.pos.service;

import com.pos.entity.SystemSetting;
import com.pos.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrialSettingsService {
    private static final String DEFAULT_TRIAL_DAYS = "default_trial_days";

    private final SystemSettingRepository repository;
    private final int fallbackDays;

    public TrialSettingsService(
            SystemSettingRepository repository,
            @Value("${subscription.default-trial-days:30}") int fallbackDays
    ) {
        this.repository = repository;
        this.fallbackDays = Math.max(fallbackDays, 0);
    }

    public int getDefaultTrialDays() {
        return repository.findById(DEFAULT_TRIAL_DAYS)
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElse(fallbackDays);
    }

    @Transactional
    public int setDefaultTrialDays(int days) {
        if (days < 0 || days > 365) {
            throw new IllegalArgumentException("Default trial days must be between 0 and 365");
        }
        SystemSetting setting = repository.findById(DEFAULT_TRIAL_DAYS).orElseGet(SystemSetting::new);
        setting.setKey(DEFAULT_TRIAL_DAYS);
        setting.setValue(String.valueOf(days));
        repository.save(setting);
        return days;
    }
}
