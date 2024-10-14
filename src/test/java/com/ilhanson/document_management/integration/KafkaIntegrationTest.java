package com.ilhanson.document_management.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.ilhanson.document_management.dtos.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaIntegrationTest {

  @Container @ServiceConnection
  public static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:latest");

  @Container
  public static KafkaContainer kafkaContainer =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @Test
  public void kafkaIntegrationTest() throws InterruptedException {
    // Step 1: Log in with an EDITOR account and retrieve the token
    LoginDTO loginDTO = new LoginDTO("kaib", "password");
    String token =
        webTestClient
            .post()
            .uri("/api/v1/auth/login")
            .bodyValue(loginDTO)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AuthenticationResponseDTO.class)
            .returnResult()
            .getResponseBody()
            .token();

    assertThat(token).isNotNull();

    // Step 2: Get the author(id=1) and its documents
    AuthorDetailsDTO author =
        webTestClient
            .get()
            .uri("/api/v1/authors/1")
            .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(AuthorDetailsDTO.class)
            .returnResult()
            .getResponseBody();

    List<DocumentDTO> authorDocuments = author.getDocuments();
    assertThat(authorDocuments).hasSize(3);

    // Step 3: Update author by replacing its 3 documents (ids 1,2, 3) with a new one (id 4)
    AuthorUpdateDTO updatedAuthor =
        new AuthorUpdateDTO(
            author.getId(),
            author.getFirstName(),
            author.getLastName(),
            List.of(new IdInputDTO(4L)));
    webTestClient
        .put()
        .uri("/api/v1/authors/" + updatedAuthor.getId())
        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
        .bodyValue(updatedAuthor)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthorDetailsDTO.class);

    // Step 4: Wait for a while so that events are published and consumed
    // A better alternative is to use a library like awaitility
    Thread.sleep(3000);

    // Step 5: Check if author (1) and documents (1, 2, 3) have been removed
    // and the document (4) is still there
    webTestClient
        .get()
        .uri("/api/v1/authors/1")
        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
        .exchange()
        .expectStatus()
        .isNotFound();

    List<DocumentDTO> documents =
        webTestClient
            .get()
            .uri("/api/v1/documents")
            .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBodyList(DocumentDTO.class)
            .returnResult()
            .getResponseBody();

    assertThat(documents).extracting(DocumentDTO::getId).doesNotContain(4L).contains(1L, 2L, 3L);
  }
}
