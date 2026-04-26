package com.erts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganiserRegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String email;
    @NotBlank private String phone;
    private String organisation;
    @NotBlank private String organiserName;  // displayed on events
    private String organiserBio;
    @NotBlank @Size(min = 8) private String password;
}
