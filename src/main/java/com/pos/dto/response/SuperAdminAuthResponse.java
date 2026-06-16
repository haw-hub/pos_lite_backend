package com.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuperAdminAuthResponse {
    private String token;
    private String username;
    private String role;
}
