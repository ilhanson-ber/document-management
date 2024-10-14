package com.ilhanson.document_management.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.ilhanson.document_management.models.Role;
import com.ilhanson.document_management.models.User;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

  @Container @ServiceConnection
  public static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:latest");

  @Autowired private UserRepository userRepository;

  @Test
  void shouldFindAllUsers() {
    List<User> users = userRepository.findAll();
    assertThat(users).hasSize(2);
  }

  @Test
  void shouldFindUserById() {
    Optional<User> user = userRepository.findById(1L);
    assertThat(user).isPresent();
    assertThat(user.get().getFirstName()).isEqualTo("Samira");
  }

  @Test
  void shouldFindUserByUsername() {
    Optional<User> user = userRepository.findByUsername("samiral");
    assertThat(user).isPresent();
    assertThat(user.get().getFirstName()).isEqualTo("Samira");
    assertThat(user.get().getRole()).isEqualTo(Role.READER);
  }

  @Test
  void shouldCreateNewUser() {
    User user =
        User.builder()
            .firstName("Alice")
            .lastName("Brown")
            .username("aliceb")
            .password("password123")
            .role(Role.READER)
            .build();

    User savedUser = userRepository.save(user);

    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getUsername()).isEqualTo("aliceb");
  }

  @Test
  void shouldNotCreateUserWithEmptyFirstName() {
    User user =
        User.builder()
            .firstName("")
            .lastName("Brown")
            .username("aliceb")
            .password("password123")
            .role(Role.READER)
            .build();

    try {
      userRepository.save(user);
      assertThat(false).isTrue(); // Should not reach this line
    } catch (Exception e) {
      assertThat(e).isInstanceOf(ConstraintViolationException.class);
    }
  }

  @Test
  void shouldNotCreateUserWithEmptyLastName() {
    User user =
        User.builder()
            .firstName("Alice")
            .lastName("")
            .username("aliceb")
            .password("password123")
            .role(Role.READER)
            .build();

    try {
      userRepository.save(user);
      assertThat(false).isTrue(); // Should not reach this line
    } catch (Exception e) {
      assertThat(e).isInstanceOf(ConstraintViolationException.class);
    }
  }

  @Test
  void shouldNotCreateUserWithDuplicateUsername() {
    User user =
        User.builder()
            .firstName("Alice")
            .lastName("Brown")
            .username("samiral") // Username already exists
            .password("password123")
            .role(Role.READER)
            .build();

    try {
      userRepository.save(user);
      assertThat(false).isTrue(); // Should not reach this line
    } catch (Exception e) {
      assertThat(e).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
  }
}
