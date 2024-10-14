package com.ilhanson.document_management.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ilhanson.document_management.config.security.JwtUtility;
import com.ilhanson.document_management.dtos.AuthenticationResponseDTO;
import com.ilhanson.document_management.dtos.LoginDTO;
import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.exceptions.DuplicateKeyException;
import com.ilhanson.document_management.mappers.UserMapper;
import com.ilhanson.document_management.models.Role;
import com.ilhanson.document_management.models.User;
import com.ilhanson.document_management.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtUtility jwtUtility;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private UserMapper userMapper;

  @InjectMocks private AuthenticationService authenticationService;

  @Test
  void shouldLoginSuccessfully() {
    // Given
    LoginDTO loginDTO = new LoginDTO("samiral", "password");
    User user = User.builder().id(1L).username("samiral").role(Role.READER).build();

    // Mocking the authentication and repository
    when(userRepository.findByUsername("samiral")).thenReturn(Optional.of(user));
    when(jwtUtility.generateToken(user)).thenReturn("token123");

    // When
    AuthenticationResponseDTO response = authenticationService.login(loginDTO);

    // Then
    assertThat(response.token()).isEqualTo("token123");

    verify(authenticationManager)
        .authenticate(new UsernamePasswordAuthenticationToken("samiral", "password"));
    verify(jwtUtility).generateToken(user);
  }

  @Test
  void shouldThrowExceptionWhenUserNotFoundOnLogin() {
    // Given
    LoginDTO loginDTO = new LoginDTO("unknownuser", "password");

    // Mocking the repository to return empty
    when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

    // Then
    assertThatThrownBy(() -> authenticationService.login(loginDTO))
        .isInstanceOf(
            RuntimeException.class); // Adjust this based on the exception you want to throw
  }

  @Test
  void shouldSignupSuccessfully() {
    // Given
    SignupDTO signupDTO = new SignupDTO();
    signupDTO.setUsername("newuser");
    signupDTO.setPassword("password");

    User user =
        User.builder().username("newuser").password("encodedPassword").role(Role.READER).build();

    // Mocking the userMapper and passwordEncoder
    when(userMapper.mapToModel(signupDTO)).thenReturn(user);
    when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
    when(userRepository.save(user)).thenReturn(user);
    when(jwtUtility.generateToken(user)).thenReturn("token123");

    // When
    AuthenticationResponseDTO response = authenticationService.signup(signupDTO);

    // Then
    assertThat(response.token()).isEqualTo("token123");

    verify(userMapper).mapToModel(signupDTO);
    verify(passwordEncoder).encode("password");
    verify(userRepository).save(user);
    verify(jwtUtility).generateToken(user);
  }

  @Test
  void shouldThrowDuplicateKeyExceptionOnSignupWhenUsernameAlreadyExists() {
    // Given
    SignupDTO signupDTO = new SignupDTO();
    signupDTO.setUsername("duplicateUser");
    signupDTO.setPassword("password");

    User user = User.builder().username("duplicateUser").password("encodedPassword").build();

    // Mocking the userMapper and passwordEncoder
    when(userMapper.mapToModel(signupDTO)).thenReturn(user);
    when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

    // Simulate a DataIntegrityViolationException for duplicate key
    when(userRepository.save(user)).thenThrow(new DataIntegrityViolationException(""));

    // Then
    assertThatThrownBy(() -> authenticationService.signup(signupDTO))
        .isInstanceOf(DuplicateKeyException.class)
        .hasMessageContaining("username");

    verify(userMapper).mapToModel(signupDTO);
    verify(passwordEncoder).encode("password");
    verify(userRepository).save(user);
  }
}
