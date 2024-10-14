package com.ilhanson.document_management.services;

import com.ilhanson.document_management.config.security.JwtUtility;
import com.ilhanson.document_management.dtos.AuthenticationResponseDTO;
import com.ilhanson.document_management.dtos.LoginDTO;
import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.exceptions.DuplicateKeyException;
import com.ilhanson.document_management.mappers.UserMapper;
import com.ilhanson.document_management.models.User;
import com.ilhanson.document_management.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {
  private final UserRepository userRepository;
  private final JwtUtility jwtUtility;

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  private final UserMapper userMapper;

  public AuthenticationResponseDTO login(LoginDTO loginDTO) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

    User user = userRepository.findByUsername(loginDTO.getUsername()).orElseThrow();
    String token = jwtUtility.generateToken(user);

    return new AuthenticationResponseDTO(token);
  }

  public AuthenticationResponseDTO signup(SignupDTO signupDTO) {
    User user = userMapper.mapToModel(signupDTO);
    user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));

    User savedUser;
    try {
      savedUser = userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateKeyException("username", signupDTO.getUsername());
    }

    String token = jwtUtility.generateToken(savedUser);
    return new AuthenticationResponseDTO(token);
  }
}
