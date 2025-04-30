package com.example.beblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login Request")
public class LoginRequest {
    @Schema(description = "Username", example = "johndoe")
    @NotBlank
    private String username;

    @Schema(description = "Password", example = "password123")
    @NotBlank
    private String password;
}