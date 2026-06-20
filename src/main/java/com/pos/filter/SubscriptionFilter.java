package com.pos.filter;

import com.pos.entity.User;
import com.pos.repository.UserRepository;
import com.pos.service.SubscriptionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class SubscriptionFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public SubscriptionFilter(
            UserRepository userRepository,
            SubscriptionService subscriptionService
    ) {
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/super-admin/")
                || path.startsWith("/api/subscription/status")
                || path.startsWith("/api/subscription/payment-proof")
                || path.startsWith("/api/test/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getShop() == null || !subscriptionService.canUseApp(user.getShop())) {
            response.setStatus(402);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"message\":\"Shop subscription has expired or is suspended\","
                            + "\"code\":\"SUBSCRIPTION_REQUIRED\"}"
            );
            return;
        }
        chain.doFilter(request, response);
    }
}
