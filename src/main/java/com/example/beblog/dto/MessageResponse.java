package com.example.beblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Message Response")
public class MessageResponse {
    @Schema(description = "Response Message", example = "User registered successfully!")
    private String message;
}