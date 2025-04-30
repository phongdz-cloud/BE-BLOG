package com.example.beblog.security;

import com.example.beblog.model.ERole;
import com.example.beblog.model.Role;
import com.example.beblog.model.User;
import com.example.beblog.repository.RoleRepository;
import com.example.beblog.repository.UserRepository;
import com.example.beblog.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create roles
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        roleRepository.save(userRole);

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        roleRepository.save(adminRole);

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidTokenProvided() throws ServletException, IOException {
        // Create test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
        user.setRoles(roles);

        userRepository.save(user);

        // Create authentication and generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.generateToken(authentication);

        // Create mock request and response
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Verify authentication is set in SecurityContext
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
        };

        // Execute filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidTokenProvided() throws ServletException, IOException {
        // Create mock request with invalid token
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Verify authentication is not set in SecurityContext
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        };

        // Execute filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenNoTokenProvided() throws ServletException, IOException {
        // Create mock request without token
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Verify authentication is not set in SecurityContext
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        };

        // Execute filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    }
}