package com.example.beblog.controller;

import com.example.beblog.common.ApiResponse;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private RoleRepository roleRepository;

        @MockBean
        private PasswordEncoder passwordEncoder;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        private User testUser;
        private Role userRole;
        private Role adminRole;
        private Role modRole;

        @BeforeEach
        void setUp() {
                userRole = new Role();
                userRole.setName(ERole.ROLE_USER);

                adminRole = new Role();
                adminRole.setName(ERole.ROLE_ADMIN);

                modRole = new Role();
                modRole.setName(ERole.ROLE_MODERATOR);

                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setPassword("encodedPassword");
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                testUser.setRoles(roles);

                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
                when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
                when(roleRepository.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.of(modRole));
                when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        }

        @Test
        void testSignupSuccess_WithDefaultRole() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(userRepository.save(any())).thenReturn(testUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));
        }

        @Test
        void testSignupSuccess_WithAdminRole() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("admin"));

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(userRepository.save(any())).thenReturn(testUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));
        }

        @Test
        void testSignupSuccess_WithModeratorRole() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("mod"));

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(userRepository.save(any())).thenReturn(testUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));
        }

        @Test
        void testSignupWithInvalidRoleShouldDefaultToUserRole() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("invalid_role"));

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

                // Create a new user that will be returned by save
                User savedUser = new User();
                savedUser.setUsername(signupRequest.getUsername());
                savedUser.setEmail(signupRequest.getEmail());
                savedUser.setPassword("encodedPassword");
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                savedUser.setRoles(roles);

                when(userRepository.save(any())).thenReturn(savedUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));

                // Verify that the user was saved with USER role
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                User capturedUser = userCaptor.getValue();

                // Verify the captured user has USER role
                assertEquals(1, capturedUser.getRoles().size());
                assertTrue(capturedUser.getRoles().stream()
                                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
        }

        @Test
        void testSignupWithNoRolesShouldDefaultToUserRole() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(null); // No roles specified

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

                // Create a new user that will be returned by save
                User savedUser = new User();
                savedUser.setUsername(signupRequest.getUsername());
                savedUser.setEmail(signupRequest.getEmail());
                savedUser.setPassword("encodedPassword");
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                savedUser.setRoles(roles);

                when(userRepository.save(any())).thenReturn(savedUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));

                // Verify that the user was saved with USER role
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                User capturedUser = userCaptor.getValue();

                // Verify the captured user has USER role
                assertEquals(1, capturedUser.getRoles().size());
                assertTrue(capturedUser.getRoles().stream()
                                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
        }

        @Test
        void testSignupWithEmptyRolesShouldDefaultToUserRole() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(new HashSet<>()); // Empty roles set

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

                // Create a new user that will be returned by save
                User savedUser = new User();
                savedUser.setUsername(signupRequest.getUsername());
                savedUser.setEmail(signupRequest.getEmail());
                savedUser.setPassword("encodedPassword");
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                savedUser.setRoles(roles);

                when(userRepository.save(any())).thenReturn(savedUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));

                // Verify that the user was saved with USER role
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                User capturedUser = userCaptor.getValue();

                // Verify the captured user has USER role
                assertEquals(1, capturedUser.getRoles().size());
                assertTrue(capturedUser.getRoles().stream()
                                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
        }

        @Test
        void testSignupWithEmptyRoles() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of());

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(userRepository.save(any())).thenReturn(testUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));
        }

        @Test
        void testSignupWithNullRoles() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(null);

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(userRepository.save(any())).thenReturn(testUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));
        }

        @Test
        void testSigninSuccess() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("password123");

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword());
                when(authenticationManager.authenticate(any())).thenReturn(authentication);
                when(userRepository.findByUsername(any())).thenReturn(Optional.of(testUser));
                when(jwtTokenProvider.generateToken(any())).thenReturn("jwt_token");

                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.token").value("jwt_token"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.username").value("testuser"))
                                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));
        }

        @Test
        void testSigninInvalidCredentials() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("wrongpassword");

                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        }

        @Test
        void testSignupWithDuplicateUsername() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");

                when(userRepository.existsByUsername(any())).thenReturn(true);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
        }

        @Test
        void testSignupWithDuplicateEmail() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(true);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
        }

        @Test
        void testSignupWithDuplicateUsernameAndEmail() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");

                when(userRepository.existsByUsername(any())).thenReturn(true);
                when(userRepository.existsByEmail(any())).thenReturn(true);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
        }

        @Test
        void testSignupWhenUserRoleNotFound() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(null); // No roles specified

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Role is not found."));
        }

        @Test
        void testSignupWhenUserRoleNotFoundWithEmptyRoles() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(new HashSet<>()); // Empty roles set

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Role is not found."));
        }

        @Test
        void testSignupWhenAdminRoleNotFound() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("admin"));

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Role is not found."));
        }

        @Test
        void testSignupWhenModeratorRoleNotFound() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("mod"));

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Role is not found."));
        }

        @Test
        void testSigninWhenUserNotFound() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("password123");

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword());
                when(authenticationManager.authenticate(any())).thenReturn(authentication);
                when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: User not found."));
        }

        @Test
        void testSigninSuccessWithMultipleRoles() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("password123");

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword());
                when(authenticationManager.authenticate(any())).thenReturn(authentication);

                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                roles.add(adminRole);
                user.setRoles(roles);

                when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
                when(jwtTokenProvider.generateToken(any())).thenReturn("jwt_token");

                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.token").value("jwt_token"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.username").value("testuser"))
                                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                                .andExpect(jsonPath("$.data.roles").isArray())
                                .andExpect(jsonPath("$.data.roles.length()").value(2))
                                .andExpect(jsonPath("$.data.roles").value(hasItems("ROLE_USER", "ROLE_ADMIN")));
        }

        @Test
        void testSigninWithNonExistentUsername() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("nonexistent");
                loginRequest.setPassword("password123");

                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        }

        @Test
        void testSigninWithWrongPassword() throws Exception {
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("wrongpassword");

                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid username or password"));
        }

        @Test
        void testSignupWithInvalidRoleInSwitchCase() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("invalid_role")); // Invalid role that will go to default case

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

                // Create a new user that will be returned by save
                User savedUser = new User();
                savedUser.setUsername(signupRequest.getUsername());
                savedUser.setEmail(signupRequest.getEmail());
                savedUser.setPassword("encodedPassword");
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                savedUser.setRoles(roles);

                when(userRepository.save(any())).thenReturn(savedUser);

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                                .andExpect(jsonPath("$.data").value("User registered successfully!"));

                // Verify that the user was saved with USER role
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                User capturedUser = userCaptor.getValue();

                // Verify the captured user has USER role
                assertEquals(1, capturedUser.getRoles().size());
                assertTrue(capturedUser.getRoles().stream()
                                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
        }

        @Test
        void testSignupWithInvalidRoleInSwitchCaseAndUserRoleNotFound() throws Exception {
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password123");
                signupRequest.setRoles(Set.of("invalid_role")); // Invalid role that will go to default case

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Error: Role is not found."));
        }
}