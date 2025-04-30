package com.example.beblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "JWT Response")
public class JwtResponse {
    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "Token Type", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "User Roles")
    private List<String> roles;

    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}