package com.ilhanson.document_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilhanson.document_management.config.JwtAuthenticationFilter;
import com.ilhanson.document_management.dtos.AuthenticationResponseDTO;
import com.ilhanson.document_management.dtos.LoginDTO;
import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.exceptions.DuplicateKeyException;
import com.ilhanson.document_management.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthenticationController.class,
        excludeFilters =
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
class AuthenticationControllerTest {

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("samiral", "password");
        AuthenticationResponseDTO responseDTO = new AuthenticationResponseDTO("token123");

        when(authenticationService.login(any(LoginDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    @WithMockUser
    void shouldSignupSuccessfully() throws Exception {
        // Arrange
        SignupDTO signupDTO = SignupDTO.builder()
                .firstName("Samira")
                .lastName("Lang")
                .username("samiral")
                .password("password123")
                .role(com.ilhanson.document_management.models.Role.READER)
                .build();

        AuthenticationResponseDTO responseDTO = new AuthenticationResponseDTO("token123");

        when(authenticationService.signup(any(SignupDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    @WithMockUser
    void shouldHandleBadCredentialsExceptionOnLogin() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("wronguser", "wrongpassword");

        // Simulate the service throwing a BadCredentialsException when incorrect credentials are provided
        when(authenticationService.login(any(LoginDTO.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())  // Should return 401 Unauthorized
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.status").value("Unauthorized"))
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidLoginDTO() throws Exception {
        // Arrange
        LoginDTO invalidLoginDTO = new LoginDTO("", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed for the request body. See errorDetails for more information."))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.errorDetails.username").value("Username can not be empty"))
                .andExpect(jsonPath("$.errorDetails.password").value("Password can not be empty"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidSignupDTO() throws Exception {
        // Arrange
        SignupDTO invalidSignupDTO = SignupDTO.builder()
                .firstName("")
                .lastName("")
                .username("")
                .password("")
                .role(null)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSignupDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed for the request body. See errorDetails for more information."))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.errorDetails.firstName").value("First name can not be empty"))
                .andExpect(jsonPath("$.errorDetails.lastName").value("Last name can not be empty"))
                .andExpect(jsonPath("$.errorDetails.username").value("Username can not be empty"))
                .andExpect(jsonPath("$.errorDetails.password").value("Password can not be empty"))
                .andExpect(jsonPath("$.errorDetails.role").value("Role can not be empty"));
    }

    @Test
    @WithMockUser
    void shouldHandleDuplicateKeyExceptionOnSignup() throws Exception {
        // Arrange
        SignupDTO signupDTO = SignupDTO.builder()
                .firstName("Samira")
                .lastName("Lang")
                .username("samiral")
                .password("password123")
                .role(com.ilhanson.document_management.models.Role.READER)
                .build();

        when(authenticationService.signup(any(SignupDTO.class))).thenThrow(new DuplicateKeyException("username", "samiral"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("username with samiral already exists"))
                .andExpect(jsonPath("$.status").value("Conflict"))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

}