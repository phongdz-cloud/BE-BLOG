package com.example.beblog.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String createdAt;
    private String updatedAt;

    public UserResponse(Long id, String username, String email, String createdAt, String updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}