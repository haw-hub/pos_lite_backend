package com.pos.service;

import com.pos.entity.User;
import com.pos.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive");
        }
        if (user.getShop() == null || !user.getShop().isActive()) {
            throw new RuntimeException("Shop account is inactive or missing");
        }
        return user;
    }
}
