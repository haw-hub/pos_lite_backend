// src/main/java/com/pos/dto/response/SignupResponse.java
package com.pos.dto.response;

import lombok.Data;

@Data
public class SignupResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String message;
}