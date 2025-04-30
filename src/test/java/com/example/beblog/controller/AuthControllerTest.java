package com.example.beblog.controller;

import com.example.beblog.config.TestConfig;
import com.example.beblog.exception.GlobalExceptionHandler;
import com.example.beblog.model.ERole;
import com.example.beblog.model.Role;
import com.example.beblog.model.User;
import com.example.beblog.payload.request.LoginRequest;
import com.example.beblog.payload.request.SignupRequest;
import com.example.beblog.repository.RoleRepository;
import com.example.beblog.repository.UserRepository;
import com.example.beblog.security.JwtTokenProvider;
import com.example.beblog.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = { TestConfig.class })
@TestPropertySource(properties = {
                "jwt.secret=testSecretKey1234567890123456789012345678901234567890",
                "jwt.expiration=86400000"
})
class AuthControllerTest {

        private MockMvc mockMvc;

        @InjectMocks
        private AuthController authController;

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private UserRepository userRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtTokenProvider jwtTokenProvider;

        @Mock
        private UserDetailsServiceImpl userDetailsService;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();

                ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver();
                exceptionResolver.afterPropertiesSet();

                GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

                ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager(
                                new HeaderContentNegotiationStrategy());

                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(globalExceptionHandler)
                                .setHandlerExceptionResolvers(exceptionResolver)
                                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                                .setContentNegotiationManager(contentNegotiationManager)
                                .build();
        }

        @Test
        void testSigninSuccess() throws Exception {
                // Arrange
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("password");

                Set<Role> roles = new HashSet<>();
                Role userRole = new Role();
                userRole.setName(ERole.ROLE_USER);
                roles.add(userRole);

                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setPassword("encodedPassword");
                user.setRoles(roles);

                Set<String> strRoles = user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet());

                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                                .username("testuser")
                                .password("encodedPassword")
                                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                                .build();

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(authentication);

                when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token");
                when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

                // Act & Assert
                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.token").value("jwt.token"))
                                .andExpect(jsonPath("$.data.type").value("Bearer"))
                                .andExpect(jsonPath("$.data.id").value(user.getId()))
                                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"))
                                .andExpect(jsonPath("$.message").value("Login successful"));

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
                verify(jwtTokenProvider).generateToken(authentication);
        }

        @Test
        void testSigninInvalidCredentials() throws Exception {
                // Arrange
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("wrongpassword");

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Invalid credentials"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Invalid username or password"));

                verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        void testSignupSuccess_WithDefaultRole() throws Exception {
                // Arrange
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password");
                signupRequest.setRoles(Collections.singleton("user"));

                Role userRole = new Role();
                userRole.setName(ERole.ROLE_USER);

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
                when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

                // Act & Assert
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User registered successfully!"));
        }

        @Test
        void testSignupWithNullRoles() throws Exception {
                // Arrange
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("testuser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password");
                signupRequest.setRoles(null);

                Role userRole = new Role();
                userRole.setName(ERole.ROLE_USER);

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
                when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

                // Act & Assert
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User registered successfully!"));
        }

        @Test
        void testSignupWithDuplicateUsername() throws Exception {
                // Arrange
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("existinguser");
                signupRequest.setEmail("test@example.com");
                signupRequest.setPassword("password");
                signupRequest.setRoles(Collections.singleton("user"));

                when(userRepository.existsByUsername("existinguser")).thenReturn(true);

                // Act & Assert
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));

                verify(userRepository).existsByUsername("existinguser");
                verify(userRepository, never()).existsByEmail(any());
                verify(userRepository, never()).save(any());
        }

        @Test
        void testSignupWithDuplicateEmail() throws Exception {
                // Arrange
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("newuser");
                signupRequest.setEmail("existing@example.com");
                signupRequest.setPassword("password");
                signupRequest.setRoles(Collections.singleton("user"));

                when(userRepository.existsByUsername("newuser")).thenReturn(false);
                when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

                // Act & Assert
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));

                verify(userRepository).existsByUsername("newuser");
                verify(userRepository).existsByEmail("existing@example.com");
                verify(userRepository, never()).save(any());
        }

        @Test
        void testSignupWithAdminRole() throws Exception {
                // Arrange
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("adminuser");
                signupRequest.setEmail("admin@example.com");
                signupRequest.setPassword("password");
                signupRequest.setRoles(Collections.singleton("admin"));

                Role adminRole = new Role();
                adminRole.setName(ERole.ROLE_ADMIN);

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
                when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

                // Act & Assert
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User registered successfully!"));

                verify(userRepository).existsByUsername("adminuser");
                verify(userRepository).existsByEmail("admin@example.com");
                verify(roleRepository).findByName(ERole.ROLE_ADMIN);
                verify(userRepository).save(any(User.class));
        }

        @Test
        void testSignupWithModeratorRole() throws Exception {
                // Arrange
                SignupRequest signupRequest = new SignupRequest();
                signupRequest.setUsername("moduser");
                signupRequest.setEmail("mod@example.com");
                signupRequest.setPassword("password");
                signupRequest.setRoles(Collections.singleton("mod"));

                Role modRole = new Role();
                modRole.setName(ERole.ROLE_MODERATOR);

                when(userRepository.existsByUsername(any())).thenReturn(false);
                when(userRepository.existsByEmail(any())).thenReturn(false);
                when(roleRepository.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.of(modRole));
                when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

                // Act & Assert
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signupRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User registered successfully!"));

                verify(userRepository).existsByUsername("moduser");
                verify(userRepository).existsByEmail("mod@example.com");
                verify(roleRepository).findByName(ERole.ROLE_MODERATOR);
                verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Test logout success")
        void testLogoutSuccess() throws Exception {
                // Arrange
                Authentication authentication = mock(Authentication.class);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Act & Assert
                mockMvc.perform(post("/api/auth/logout"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Success"));

                // Verify that the security context was cleared
                assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("Test logout when not authenticated")
        void testLogoutWhenNotAuthenticated() throws Exception {
                // Arrange
                SecurityContextHolder.clearContext();

                // Act & Assert
                mockMvc.perform(post("/api/auth/logout"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Success"));

                // Verify that the security context remains cleared
                assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
}