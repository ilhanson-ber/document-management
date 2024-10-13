package com.ilhanson.document_management.integration;

import com.ilhanson.document_management.dtos.*;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CombinedIntegrationTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void completeIntegrationTest() {
        // Step 1: Log in with an EDITOR account and retrieve the token
        LoginDTO loginDTO = new LoginDTO("kaib", "password");
        String token = webTestClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponseDTO.class)
                .returnResult().getResponseBody()
                .token();

        assertThat(token).isNotNull();

        // Step 2: Create an author with documents 1 and 2, using the token
        List<DocumentDTO> documents = webTestClient.get()
                .uri("/api/v1/documents")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DocumentDTO.class)
                .returnResult().getResponseBody();
        assertThat(documents).hasSizeGreaterThan(1);

        DocumentDTO doc1 = documents.get(0);
        DocumentDTO doc2 = documents.get(1);

        AuthorCreateDTO newAuthor = new AuthorCreateDTO(null, "John", "Doe", List.of(new IdInputDTO(doc1.getId()), new IdInputDTO(doc2.getId())));
        AuthorDetailsDTO createdAuthor = webTestClient.post()
                .uri("/api/v1/authors")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(newAuthor)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthorDetailsDTO.class)
                .returnResult().getResponseBody();

        assertThat(createdAuthor).isNotNull();
        assertThat(createdAuthor.getDocuments()).extracting("id").containsExactlyInAnyOrder(doc1.getId(), doc2.getId());

        // Step 3: Create a new document with no authors and no references, using the token
        DocumentCreateDTO newDocument = new DocumentCreateDTO(null, "New Doc", "This is a new document", new ArrayList<>(), new ArrayList<>());
        DocumentDetailsDTO createdDocument = webTestClient.post()
                .uri("/api/v1/documents")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(newDocument)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DocumentDetailsDTO.class)
                .returnResult().getResponseBody();

        assertThat(createdDocument).isNotNull();
        assertThat(createdDocument.getAuthors()).isEmpty();
        assertThat(createdDocument.getReferences()).isEmpty();

        // Step 4: Add the new author and document id 1 as references to this document using update, using the token
        DocumentUpdateDTO updatedDocument = new DocumentUpdateDTO(createdDocument.getId(), createdDocument.getTitle(), createdDocument.getBody(), List.of(new IdInputDTO(doc1.getId())), List.of(new IdInputDTO(createdAuthor.getId())));
        webTestClient.put()
                .uri("/api/v1/documents/" + createdDocument.getId())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(updatedDocument)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DocumentDetailsDTO.class)
                .consumeWith(response -> {
                    DocumentDetailsDTO updated = response.getResponseBody();
                    assertThat(updated.getReferences()).hasSize(1).extracting("id").containsExactly(doc1.getId());
                    assertThat(updated.getAuthors()).hasSize(1).extracting("id").containsExactly(createdAuthor.getId());
                });

        // Step 5: Update the new author's name and remove document 2, using the token
        // This author and the associated documents(s) will be deleted by Kafka services automatically
        // Kafka flow is checked within KafkaIntegrationTest
        AuthorUpdateDTO updatedAuthor = new AuthorUpdateDTO(createdAuthor.getId(), "UpdatedJohn", "UpdatedDoe", List.of(new IdInputDTO(doc1.getId())));
        webTestClient.put()
                .uri("/api/v1/authors/" + createdAuthor.getId())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(updatedAuthor)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthorDetailsDTO.class)
                .consumeWith(response -> {
                    AuthorDetailsDTO author = response.getResponseBody();
                    assertThat(author.getFirstName()).isEqualTo("UpdatedJohn");
                    assertThat(author.getLastName()).isEqualTo("UpdatedDoe");
                    assertThat(author.getDocuments()).hasSize(1).extracting("id").containsExactly(doc1.getId());
                });


        // Step 6: Delete an author, using the token
        webTestClient.delete()
                .uri("/api/v1/authors/1")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isNoContent();

        // Step 7: Delete a document, using the token
        webTestClient.delete()
                .uri("/api/v1/documents/2")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isNoContent();

        // Step 8: Make a get details request to check 404 for the given author, using the token
        webTestClient.get()
                .uri("/api/v1/authors/1")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isNotFound();

        // Step 9: Make a get details request to check 404 for the document, using the token
        webTestClient.get()
                .uri("/api/v1/documents/2")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .exchange()
                .expectStatus().isNotFound();
    }

}
