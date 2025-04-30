package com.example.beblog.controller;

import com.example.beblog.dto.LoginRequest;
import com.example.beblog.dto.SignupRequest;
import com.example.beblog.model.ERole;
import com.example.beblog.model.Role;
import com.example.beblog.model.User;
import com.example.beblog.repository.RoleRepository;
import com.example.beblog.repository.UserRepository;
import com.example.beblog.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

        Role modRole = new Role();
        modRole.setName(ERole.ROLE_MODERATOR);
        roleRepository.save(modRole);
    }

    @Test
    void testSignupSuccess_WithDefaultRole() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRoles(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with correct role
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
    }

    @Test
    void testSignupSuccess_WithAdminRole() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("adminuser");
        signupRequest.setEmail("admin@example.com");
        signupRequest.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        signupRequest.setRoles(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with admin role
        User savedUser = userRepository.findByUsername("adminuser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN));
    }

    @Test
    void testSignupSuccess_WithModeratorRole() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("moduser");
        signupRequest.setEmail("mod@example.com");
        signupRequest.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("mod");
        signupRequest.setRoles(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with moderator role
        User savedUser = userRepository.findByUsername("moduser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_MODERATOR));
    }

    @Test
    void testSignupDuplicateUsername() throws Exception {
        // Create a user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
        user.setRoles(roles);
        userRepository.save(user);

        // Try to signup with the same username
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("another@example.com");
        signupRequest.setPassword("password123");
        Set<String> newRoles = new HashSet<>();
        newRoles.add("user");
        signupRequest.setRoles(newRoles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void testSignupDuplicateEmail() throws Exception {
        // Create a user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
        user.setRoles(roles);
        userRepository.save(user);

        // Try to signup with the same email
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("anotheruser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        Set<String> newRoles = new HashSet<>();
        newRoles.add("user");
        signupRequest.setRoles(newRoles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
    }

    @Test
    void testSignupWithInvalidRole() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("invalid_role");
        signupRequest.setRoles(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with default USER role
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
    }

    @Test
    void testSigninSuccess() throws Exception {
        // Create a user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
        user.setRoles(roles);
        userRepository.save(user);

        // Try to signin
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void testSigninInvalidCredentials() throws Exception {
        // Create a user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
        user.setRoles(roles);
        userRepository.save(user);

        // Try to signin with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSignupWithNoRoles() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        // Không set roles (để strRoles là null)

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with default USER role
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
    }

    @Test
    void testSignupWithEmptyRoles() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        // Set roles là empty set
        signupRequest.setRoles(new HashSet<>());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with default USER role
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
    }

    @Test
    void testSignupWithNullRoles() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        // Set roles là null
        signupRequest.setRoles(null);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created with default USER role
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertTrue(savedUser.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
    }
}