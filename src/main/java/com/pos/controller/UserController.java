package com.pos.controller;

import com.pos.dto.request.EmployeeRequest;
import com.pos.dto.response.UserResponse;
import com.pos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getShopUsers() {
        return userService.getShopUsers(username());
    }

    @PostMapping
    public UserResponse createEmployee(@Valid @RequestBody EmployeeRequest request) {
        return userService.createEmployee(request, username());
    }

    @PutMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestParam boolean value) {
        return userService.setActive(id, value, username());
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
