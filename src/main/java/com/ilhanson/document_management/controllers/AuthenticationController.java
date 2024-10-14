package com.ilhanson.document_management.controllers;

import com.ilhanson.document_management.dtos.AuthenticationResponseDTO;
import com.ilhanson.document_management.dtos.LoginDTO;
import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  public AuthenticationResponseDTO login(@Valid @RequestBody LoginDTO loginDTO) {
    return authenticationService.login(loginDTO);
  }

  @PostMapping("/signup")
  public AuthenticationResponseDTO signup(@Valid @RequestBody SignupDTO signupDTO) {
    return authenticationService.signup(signupDTO);
  }
}
