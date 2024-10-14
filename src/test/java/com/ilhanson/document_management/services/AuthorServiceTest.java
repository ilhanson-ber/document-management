package com.ilhanson.document_management.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.mappers.AuthorMapper;
import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import com.ilhanson.document_management.producers.AuthorEventProducer;
import com.ilhanson.document_management.repositories.AuthorRepository;
import com.ilhanson.document_management.repositories.DocumentRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

  @Mock private AuthorRepository authorRepository;

  @Mock private DocumentRepository documentRepository;

  @Mock private AuthorMapper authorMapper;

  @Mock private AuthorEventProducer authorEventProducer;

  @InjectMocks private AuthorService authorService;

  @Test
  void shouldGetAuthorDetails() {
    // Arrange
    Author author = new Author(1L, "John", "Doe", null);
    AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", null);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
    when(authorMapper.mapToDetailsDTO(author)).thenReturn(authorDetailsDTO);

    // Act
    AuthorDetailsDTO result = authorService.getAuthorDetails(1L);

    // Assert
    assertThat(result).isEqualTo(authorDetailsDTO);
    verify(authorRepository, times(1)).findById(1L);
    verify(authorMapper, times(1)).mapToDetailsDTO(author);
  }

  @Test
  void shouldGetAllAuthors() {
    // Arrange
    Author author = new Author(1L, "John", "Doe", null);
    AuthorDTO authorDTO = new AuthorDTO(1L, "John", "Doe");
    when(authorRepository.findAll()).thenReturn(List.of(author));
    when(authorMapper.mapToDTO(author)).thenReturn(authorDTO);

    // Act
    List<AuthorDTO> result = authorService.getAllAuthors();

    // Assert
    assertThat(result).containsExactly(authorDTO);
    verify(authorRepository, times(1)).findAll();
    verify(authorMapper, times(1)).mapToDTO(author);
  }

  @Test
  void shouldDeleteAuthor() {
    // Arrange
    Author author = new Author(1L, "John", "Doe", null);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

    // Act
    authorService.deleteAuthor(1L);

    // Assert
    verify(authorRepository, times(1)).delete(author);
    verify(authorEventProducer, times(1)).sendAuthorDeletedEvent(1L);
  }

  @Test
  void shouldCreateAuthor() {
    // Arrange
    AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", new ArrayList<>());
    Author authorInput = new Author(null, "John", "Doe", new HashSet<>());
    Author savedAuthor = new Author(1L, "John", "Doe", new HashSet<>());
    AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", new ArrayList<>());

    when(authorMapper.mapToModel(authorCreateDTO)).thenReturn(authorInput);
    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);
    when(authorMapper.mapToDetailsDTO(savedAuthor)).thenReturn(authorDetailsDTO);

    // Act
    AuthorDetailsDTO result = authorService.createAuthor(authorCreateDTO);

    // Assert
    assertThat(result).isEqualTo(authorDetailsDTO);
    verify(authorRepository, times(1)).save(any(Author.class));
    verify(authorMapper, times(1)).mapToDetailsDTO(savedAuthor);
  }

  @Test
  void shouldUpdateAuthor() {
    // Arrange
    AuthorUpdateDTO authorUpdateDTO = new AuthorUpdateDTO(1L, "John", "Doe", new ArrayList<>());
    Author authorInput = new Author(1L, "John", "Doe", new HashSet<>());
    Author existingAuthor = new Author(1L, "John", "Doe", new HashSet<>());
    Author savedAuthor = new Author(1L, "John", "Doe", new HashSet<>());
    AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", new ArrayList<>());

    when(authorMapper.mapToModel(authorUpdateDTO)).thenReturn(authorInput);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(existingAuthor));
    when(authorRepository.save(existingAuthor)).thenReturn(savedAuthor);
    when(authorMapper.mapToDetailsDTO(savedAuthor)).thenReturn(authorDetailsDTO);

    // Act
    AuthorDetailsDTO result = authorService.updateAuthor(authorUpdateDTO);

    // Assert
    assertThat(result).isEqualTo(authorDetailsDTO);
    verify(authorRepository, times(1)).save(existingAuthor);
    verify(authorMapper, times(1)).mapToDetailsDTO(savedAuthor);
    verify(authorEventProducer, times(1)).sendAuthorUpdatedEvent(result);
  }

  @Test
  void shouldThrowWhenAuthorNotFound() {
    // Arrange
    when(authorRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> authorService.getAuthorDetails(1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author")
        .hasMessageContaining("1");
  }

  @Test
  void shouldThrowWhenDocumentsNotFound() {
    // Arrange
    Document doc1 = new Document(1L, "Doc 1", "Body 1", new HashSet<>(), null, null);
    Document doc2 = new Document(2L, "Doc 2", "Body 2", new HashSet<>(), null, null);
    Set<Document> documentsFromClient = Set.of(doc1, doc2);
    List<Document> documentsFromRepo = List.of(doc1);
    IdInputDTO id1 = new IdInputDTO(1L);
    IdInputDTO id2 = new IdInputDTO(2L);

    AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", List.of(id1, id2));
    Author authorInput = new Author(null, "John", "Doe", documentsFromClient);

    when(authorMapper.mapToModel(authorCreateDTO)).thenReturn(authorInput);
    when(documentRepository.findAllById(anyList())).thenReturn(documentsFromRepo);

    // Act & Assert
    assertThatThrownBy(() -> authorService.createAuthor(authorCreateDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Document")
        .hasMessageContaining("2");
  }

  @Test
  void shouldCreateAuthorWithDocuments() {
    // Arrange
    Document doc1 = new Document(1L, "Doc 1", "Body 1", new HashSet<>(), null, null);
    Document doc2 = new Document(2L, "Doc 2", "Body 2", new HashSet<>(), null, null);
    Set<Document> documentsFromClient = Set.of(doc1, doc2);
    List<Document> documentsFromRepo = List.of(doc1, doc2);
    IdInputDTO id1 = new IdInputDTO(1L);
    IdInputDTO id2 = new IdInputDTO(2L);

    AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", List.of(id1, id2));
    Author authorInput = new Author(null, "John", "Doe", documentsFromClient);
    Author savedAuthor = new Author(1L, "John", "Doe", documentsFromClient);
    AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", null);

    when(authorMapper.mapToModel(authorCreateDTO)).thenReturn(authorInput);
    when(documentRepository.findAllById(anyList())).thenReturn(documentsFromRepo);
    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);
    when(authorMapper.mapToDetailsDTO(savedAuthor)).thenReturn(authorDetailsDTO);

    // Act
    AuthorDetailsDTO result = authorService.createAuthor(authorCreateDTO);

    // Assert
    verify(documentRepository, times(1)).findAllById(anyList());
    verify(authorRepository, times(1)).save(any(Author.class));
    assertThat(result).isEqualTo(authorDetailsDTO);
  }

  @Test
  void shouldUpdateAuthorWithDocuments() {
    // Arrange
    Document doc1 = new Document(1L, "Doc 1", "Body 1", new HashSet<>(), null, null);
    Document doc2 = new Document(2L, "Doc 2", "Body 2", new HashSet<>(), null, null);
    Document doc3 = new Document(3L, "Doc 3", "Body 3", new HashSet<>(), null, null);
    Set<Document> existingAuthorDocuments = new HashSet<>();
    existingAuthorDocuments.add(doc3);
    Set<Document> documentsFromClient = Set.of(doc1, doc2);
    List<Document> documentsFromRepo = List.of(doc1, doc2);

    AuthorUpdateDTO authorUpdateDTO = new AuthorUpdateDTO(1L, "John", "Doe", null);
    Author authorInput = new Author(1L, "John", "Doe", documentsFromClient);
    Author existingAuthor = new Author(1L, "John", "Doe", existingAuthorDocuments);
    Author savedAuthor = new Author(1L, "John", "Doe", documentsFromClient);
    AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", null);

    when(authorMapper.mapToModel(authorUpdateDTO)).thenReturn(authorInput);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(existingAuthor));
    when(documentRepository.findAllById(anyList())).thenReturn(documentsFromRepo);
    when(authorRepository.save(existingAuthor)).thenReturn(savedAuthor);
    when(authorMapper.mapToDetailsDTO(savedAuthor)).thenReturn(authorDetailsDTO);

    // Act
    AuthorDetailsDTO result = authorService.updateAuthor(authorUpdateDTO);

    // Assert
    verify(documentRepository, times(1)).findAllById(anyList());
    verify(authorRepository, times(1)).save(existingAuthor);
    assertThat(result).isEqualTo(authorDetailsDTO);
    verify(authorEventProducer, times(1)).sendAuthorUpdatedEvent(result);
  }
}
