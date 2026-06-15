package com.pos.service;

import com.pos.dto.request.EmployeeRequest;
import com.pos.dto.response.UserResponse;
import com.pos.entity.User;
import com.pos.enums.UserRole;
import com.pos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CurrentUserService currentUserService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getShopUsers(String username) {
        User admin = requireAdmin(username);
        return userRepository.findByShopIdOrderByCreatedAtAsc(admin.getShop().getId())
                .stream().map(UserResponse::from).toList();
    }

    public UserResponse createEmployee(EmployeeRequest request, String username) {
        User admin = requireAdmin(username);
        if (request.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Employee role must be MANAGER or CASHIER");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User employee = new User();
        employee.setUsername(request.getUsername().trim().toLowerCase());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());
        employee.setShop(admin.getShop());
        employee.setActive(true);
        return UserResponse.from(userRepository.save(employee));
    }

    public UserResponse setActive(Long id, boolean active, String username) {
        User admin = requireAdmin(username);
        User employee = userRepository.findByIdAndShopId(id, admin.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        if (employee.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("Admin cannot disable their own account");
        }
        employee.setActive(active);
        return UserResponse.from(userRepository.save(employee));
    }

    private User requireAdmin(String username) {
        User user = currentUserService.require(username);
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Admin permission required");
        }
        return user;
    }
}
