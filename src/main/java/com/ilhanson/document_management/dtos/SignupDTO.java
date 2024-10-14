package com.ilhanson.document_management.dtos;

import com.ilhanson.document_management.models.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupDTO {

  @Null(message = "ID should be null or missing when creating a user")
  private Long id;

  @NotBlank(message = "First name can not be empty")
  @Size(max = 100, message = "First name can not be longer than 100 characters")
  private String firstName;

  @NotBlank(message = "Last name can not be empty")
  @Size(max = 100, message = "Last name can not be longer than 100 characters")
  private String lastName;

  @NotBlank(message = "Username can not be empty")
  @Size(max = 100, message = "Username can not be longer than 100 characters")
  private String username;

  @NotBlank(message = "Password can not be empty")
  private String password;

  @NotNull(message = "Role can not be empty")
  private Role role;
}
