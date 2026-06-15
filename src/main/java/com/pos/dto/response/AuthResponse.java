package com.pos.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String role;
    private String fullName;
    private Long shopId;
    private String shopName;
}
