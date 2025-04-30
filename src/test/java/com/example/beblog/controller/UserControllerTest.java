package com.example.beblog.controller;

import com.example.beblog.dto.UserResponse;
import com.example.beblog.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnOkResponse() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        UserResponse mockResponse = new UserResponse(
                1L,
                "testuser",
                "test@example.com",
                now.format(formatter),
                now.format(formatter));

        when(userService.getCurrentUser()).thenReturn(mockResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.getCurrentUser();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockResponse.getId(), response.getBody().getId());
        assertEquals(mockResponse.getUsername(), response.getBody().getUsername());
        assertEquals(mockResponse.getEmail(), response.getBody().getEmail());
        assertEquals(mockResponse.getCreatedAt(), response.getBody().getCreatedAt());
        assertEquals(mockResponse.getUpdatedAt(), response.getBody().getUpdatedAt());

        verify(userService).getCurrentUser();
    }

    @Test
    void getCurrentUser_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(userService.getCurrentUser()).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userController.getCurrentUser());
        verify(userService).getCurrentUser();
    }
}