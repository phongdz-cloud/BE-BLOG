package com.example.beblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Signup Request")
public class SignupRequest {
    @Schema(description = "Username", example = "johndoe")
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @Schema(description = "Email", example = "john.doe@example.com")
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Schema(description = "Roles", example = "[\"user\"]")
    private Set<String> roles;

    @Schema(description = "Password", example = "password123")
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}