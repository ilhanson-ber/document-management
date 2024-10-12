package com.ilhanson.document_management.integration;

import com.ilhanson.document_management.dtos.AuthenticationResponseDTO;
import com.ilhanson.document_management.dtos.LoginDTO;
import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.models.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void securityIntegrationTest() {
        // Step 1: Call GET /api/v1/authors and expect 401 Unauthorized
        webTestClient.get()
                .uri("/api/v1/authors")
                .exchange()
                .expectStatus().isUnauthorized();

        // Step 2: Attempt login with wrong credentials, expect 401 Unauthorized
        LoginDTO wrongLoginDTO = new LoginDTO("wronguser", "wrongpassword");
        webTestClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(wrongLoginDTO)
                .exchange()
                .expectStatus().isUnauthorized();

        // Step 3: Login with correct credentials, expect success and receive token
        LoginDTO correctLoginDTO = new LoginDTO("samiral", "password");
        String token = webTestClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(correctLoginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponseDTO.class)
                .returnResult().getResponseBody()
                .token();

        assertThat(token).isNotNull();

        // Step 4: Use the token to make an authorized GET /api/v1/authors request, expect 200 OK
        webTestClient.get()
                .uri("/api/v1/authors")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk();

        // Step 5: Attempt to DELETE /api/v1/authors/1 with READER role, expect 403 Forbidden
        webTestClient.delete()
                .uri("/api/v1/authors/1")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isForbidden();

        // Step 6: Signup a new account with EDITOR role
        SignupDTO editorSignupDTO = SignupDTO.builder()
                .firstName("Editor")
                .lastName("User")
                .username("editoruser")
                .password("password123")
                .role(Role.EDITOR)
                .build();

        String editorToken = webTestClient.post()
                .uri("/api/v1/auth/signup")
                .bodyValue(editorSignupDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponseDTO.class)
                .returnResult().getResponseBody()
                .token();

        assertThat(editorToken).isNotNull();

        // Step 7: Use the editor token to DELETE /api/v1/authors/1 and expect noContent (204)
        webTestClient.delete()
                .uri("/api/v1/authors/1")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(editorToken))
                .exchange()
                .expectStatus().isNoContent();
    }

}
