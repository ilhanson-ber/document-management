package com.ilhanson.document_management.consumers;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.dtos.DocumentDTO;
import com.ilhanson.document_management.mappers.AuthorMapper;
import com.ilhanson.document_management.services.AuthorService;
import com.ilhanson.document_management.services.DocumentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorEventConsumerTest {

  @Mock private AuthorService authorService;

  @Mock private DocumentService documentService;

  @Mock private AuthorMapper authorMapper;

  @InjectMocks private AuthorEventConsumer authorEventConsumer;

  @Test
  void shouldHandleAuthorUpdatedEventAndCallRelevantServices() throws JsonProcessingException {
    // Arrange
    String event =
        "{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"documents\":[{\"id\":1,\"title\":\"Doc 1\",\"body\":\"Body 1\"},{\"id\":2,\"title\":\"Doc 2\",\"body\":\"Body 2\"}]}";

    AuthorDetailsDTO authorDetailsDTO =
        new AuthorDetailsDTO(
            1L,
            "John",
            "Doe",
            List.of(
                new DocumentDTO(1L, "Doc 1", "Body 1"), new DocumentDTO(2L, "Doc 2", "Body 2")));

    // Mock the mapper to return the expected AuthorDetailsDTO from the JSON event
    when(authorMapper.toDetailsDTO(event)).thenReturn(authorDetailsDTO);

    // Act
    authorEventConsumer.handleAuthorUpdated(event);

    // Assert
    // Verify that deleteDocument is called for both documents
    verify(documentService, times(1)).deleteDocument(1L);
    verify(documentService, times(1)).deleteDocument(2L);

    // Verify that deleteAuthor is called after processing the documents
    verify(authorService, times(1)).deleteAuthor(1L);
  }

  @Test
  void shouldLogErrorIfDocumentDeletionFails() throws JsonProcessingException {
    // Arrange
    String event =
        "{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"documents\":[{\"id\":1,\"title\":\"Doc 1\",\"body\":\"Body 1\"}]}";

    AuthorDetailsDTO authorDetailsDTO =
        new AuthorDetailsDTO(1L, "John", "Doe", List.of(new DocumentDTO(1L, "Doc 1", "Body 1")));

    when(authorMapper.toDetailsDTO(event)).thenReturn(authorDetailsDTO);

    // Simulate an exception when trying to delete a document
    doThrow(new RuntimeException("Failed to delete document"))
        .when(documentService)
        .deleteDocument(1L);

    // Act
    authorEventConsumer.handleAuthorUpdated(event);

    // Assert
    verify(documentService, times(1)).deleteDocument(1L);
    verify(authorService, times(1)).deleteAuthor(1L);
  }

  @Test
  void shouldLogErrorIfAuthorDeletionFails() throws JsonProcessingException {
    // Arrange
    String event =
        "{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"documents\":[{\"id\":1,\"title\":\"Doc 1\",\"body\":\"Body 1\"}]}";

    AuthorDetailsDTO authorDetailsDTO =
        new AuthorDetailsDTO(1L, "John", "Doe", List.of(new DocumentDTO(1L, "Doc 1", "Body 1")));

    when(authorMapper.toDetailsDTO(event)).thenReturn(authorDetailsDTO);

    // Simulate an exception when trying to delete the author
    doThrow(new RuntimeException("Failed to delete author")).when(authorService).deleteAuthor(1L);

    // Act
    authorEventConsumer.handleAuthorUpdated(event);

    // Assert
    verify(documentService, times(1)).deleteDocument(1L);
    verify(authorService, times(1)).deleteAuthor(1L);
  }
}
