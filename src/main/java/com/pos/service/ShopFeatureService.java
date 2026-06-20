package com.pos.service;

import com.pos.entity.User;
import com.pos.enums.ShopFeature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShopFeatureService {
    private final CurrentUserService currentUserService;

    public ShopFeatureService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public void require(String username, ShopFeature feature) {
        User user = currentUserService.require(username);
        if (!user.getShop().getEnabledFeatures().contains(feature)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Feature not enabled: " + feature.name());
        }
    }
}
