// src/main/java/com/pos/dto/request/SignupRequest.java
package com.pos.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "Username may contain only letters, numbers, dot, underscore, and hyphen")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone number must be 7-15 digits")
    private String phone;

    @Size(max = 255)
    private String shopName;
}
