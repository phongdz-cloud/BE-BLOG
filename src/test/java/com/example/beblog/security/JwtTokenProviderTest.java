package com.example.beblog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.example.beblog.config.TestConfig;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(properties = {
                "jwt.secret=testSecretKey1234567890123456789012345678901234567890",
                "jwt.expiration=86400000"
})
class JwtTokenProviderTest {

        @InjectMocks
        private JwtTokenProvider jwtTokenProvider;

        @Mock
        private Authentication authentication;

        private UserDetails userDetails;

        @BeforeEach
        void setUp() {
                jwtTokenProvider.setJwtSecret("testSecretKey1234567890123456789012345678901234567890");
                jwtTokenProvider.setJwtExpiration(86400000);

                userDetails = User.builder()
                                .username("testuser")
                                .password("password")
                                .authorities(Collections.singletonList(() -> "ROLE_USER"))
                                .build();
        }

        @Test
        void generateToken_ShouldReturnValidToken() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);

                // Act
                String token = jwtTokenProvider.generateToken(authentication);

                // Assert
                assertNotNull(token);
                assertTrue(jwtTokenProvider.validateToken(token));
                assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
        }

        @Test
        void validateToken_InvalidToken_ReturnsFalse() {
                // Act & Assert
                assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
        }

        @Test
        void getUsernameFromToken_ValidToken_ReturnsUsername() {
                // Arrange
                when(authentication.getPrincipal()).thenReturn(userDetails);
                String token = jwtTokenProvider.generateToken(authentication);

                // Act
                String username = jwtTokenProvider.getUsernameFromToken(token);

                // Assert
                assertEquals("testuser", username);
        }
}