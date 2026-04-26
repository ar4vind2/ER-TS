// ─── RegisterRequest ────────────────────────────────────────────────────────
package com.erts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    @NotBlank private String phone;
    private String organisation;
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
