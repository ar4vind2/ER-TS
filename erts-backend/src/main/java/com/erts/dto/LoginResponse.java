package com.erts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Returned after successful login
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
