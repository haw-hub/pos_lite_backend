package com.pos.dto.request;

import com.pos.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmployeeRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "Username format is invalid")
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String fullName;

    private String email;
    private String phone;

    @NotNull
    private UserRole role;
}
