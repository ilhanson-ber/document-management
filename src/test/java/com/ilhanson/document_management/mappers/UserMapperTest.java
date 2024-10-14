package com.ilhanson.document_management.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.models.Role;
import com.ilhanson.document_management.models.User;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

class UserMapperTest {

  private final ModelMapper modelMapper = new ModelMapper();
  private final UserMapper userMapper = new UserMapper(modelMapper);

  @Test
  void shouldMapSignupDTOToUser() {
    // Given
    SignupDTO signupDTO =
        SignupDTO.builder()
            .firstName("Samira")
            .lastName("Lang")
            .username("samiral")
            .password("password123")
            .role(Role.READER)
            .build();

    // When
    User user = userMapper.mapToModel(signupDTO);

    // Then
    assertThat(user).isNotNull();
    assertThat(user.getId()).isNull();
    assertThat(user.getFirstName()).isEqualTo("Samira");
    assertThat(user.getLastName()).isEqualTo("Lang");
    assertThat(user.getUsername()).isEqualTo("samiral");
    assertThat(user.getPassword()).isEqualTo("password123");
    assertThat(user.getRole()).isEqualTo(Role.READER);
  }

  @Test
  void shouldMapSignupDTOWithAllFields() {
    // Given
    SignupDTO signupDTO =
        SignupDTO.builder()
            .firstName("Kai")
            .lastName("Bennett")
            .username("kaib")
            .password("password456")
            .role(Role.EDITOR)
            .build();

    // When
    User user = userMapper.mapToModel(signupDTO);

    // Then
    assertThat(user).isNotNull();
    assertThat(user.getFirstName()).isEqualTo("Kai");
    assertThat(user.getLastName()).isEqualTo("Bennett");
    assertThat(user.getUsername()).isEqualTo("kaib");
    assertThat(user.getPassword()).isEqualTo("password456");
    assertThat(user.getRole()).isEqualTo(Role.EDITOR);
  }
}
