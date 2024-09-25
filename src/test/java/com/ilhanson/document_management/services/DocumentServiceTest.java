package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.DocumentCreateDTO;
import com.ilhanson.document_management.dtos.DocumentDTO;
import com.ilhanson.document_management.dtos.DocumentDetailsDTO;
import com.ilhanson.document_management.dtos.DocumentUpdateDTO;
import com.ilhanson.document_management.exceptions.AssociationConflictException;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.mappers.DocumentMapper;
import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import com.ilhanson.document_management.repositories.AuthorRepository;
import com.ilhanson.document_management.repositories.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void shouldGetDocumentDetails() {
        // Arrange
        Document document = new Document(1L, "Doc 1", "Body 1", null, null, null);
        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1", null, null);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentMapper.mapToDetailsDTO(document)).thenReturn(documentDetailsDTO);

        // Act
        DocumentDetailsDTO result = documentService.getDocumentDetails(1L);

        // Assert
        assertThat(result).isEqualTo(documentDetailsDTO);
        verify(documentRepository, times(1)).findById(1L);
        verify(documentMapper, times(1)).mapToDetailsDTO(document);
    }

    @Test
    void shouldGetAllDocuments() {
        // Arrange
        Document document = new Document(1L, "Doc 1", "Body 1", null, null, null);
        DocumentDTO documentDTO = new DocumentDTO(1L, "Doc 1", "Body 1");
        when(documentRepository.findAll()).thenReturn(List.of(document));
        when(documentMapper.mapToDTO(document)).thenReturn(documentDTO);

        // Act
        List<DocumentDTO> result = documentService.getAllDocuments();

        // Assert
        assertThat(result).containsExactly(documentDTO);
        verify(documentRepository, times(1)).findAll();
        verify(documentMapper, times(1)).mapToDTO(document);
    }

    @Test
    void shouldDeleteDocument() {
        // Arrange
        Document document = new Document(1L, "Doc 1", "Body 1", null, null, null);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        // Act
        documentService.deleteDocument(1L);

        // Assert
        verify(documentRepository, times(1)).delete(document);
    }

    @Test
    void shouldCreateDocument() {
        // Arrange
        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", null, null);
        Document documentInput = new Document(null, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>());
        Document savedDocument = new Document(1L, "Doc 1", "Body 1", null, null, null);
        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1", null, null);

        when(documentMapper.mapToModel(documentCreateDTO)).thenReturn(documentInput);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(documentMapper.mapToDetailsDTO(savedDocument)).thenReturn(documentDetailsDTO);

        // Act
        DocumentDetailsDTO result = documentService.createDocument(documentCreateDTO);

        // Assert
        assertThat(result).isEqualTo(documentDetailsDTO);
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(documentMapper, times(1)).mapToDetailsDTO(savedDocument);
    }

    @Test
    void shouldUpdateDocument() {
        // Arrange
        DocumentUpdateDTO documentUpdateDTO = new DocumentUpdateDTO(1L, "Doc 1", "Body 1", null, null);
        Document documentInput = new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>());
        Document existingDocument = new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>());
        Document savedDocument = new Document(1L, "Doc 1", "Body 1", null, null, null);
        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1", null, null);

        when(documentMapper.mapToModel(documentUpdateDTO)).thenReturn(documentInput);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existingDocument));
        when(documentRepository.save(existingDocument)).thenReturn(savedDocument);
        when(documentMapper.mapToDetailsDTO(savedDocument)).thenReturn(documentDetailsDTO);

        // Act
        DocumentDetailsDTO result = documentService.updateDocument(documentUpdateDTO);

        // Assert
        assertThat(result).isEqualTo(documentDetailsDTO);
        verify(documentRepository, times(1)).save(existingDocument);
        verify(documentMapper, times(1)).mapToDetailsDTO(savedDocument);
    }

    @Test
    void shouldThrowWhenDocumentNotFound() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentService.getDocumentDetails(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document")
                .hasMessageContaining("1");
    }

    @Test
    void shouldCreateDocumentWithAuthorsAndReferences() {
        // Arrange
        Author author1 = new Author(1L, "John", "Doe", new HashSet<>());
        Author author2 = new Author(2L, "Jane", "Doe", new HashSet<>());
        Set<Author> authorsFromClient = Set.of(author1, author2);
        List<Author> authorsFromRepo = List.of(author1, author2);

        Document reference1 = new Document(2L, "Ref 1", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>());
        Document reference2 = new Document(3L, "Ref 2", "Body 3", new HashSet<>(), new HashSet<>(), new HashSet<>());
        Set<Document> referencesFromClient = Set.of(reference1, reference2);
        List<Document> referencesFromRepo = List.of(reference1, reference2);

        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", null, null);
        Document documentInput = new Document(null, "Doc 1", "Body 1", authorsFromClient, referencesFromClient, null);
        Document savedDocument = new Document(1L, "Doc 1", "Body 1", authorsFromClient, referencesFromClient, null);
        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1", null, null);

        when(documentMapper.mapToModel(documentCreateDTO)).thenReturn(documentInput);
        when(authorRepository.findAllById(anyList())).thenReturn(authorsFromRepo);
        when(documentRepository.findAllById(anyList())).thenReturn(referencesFromRepo);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(documentMapper.mapToDetailsDTO(savedDocument)).thenReturn(documentDetailsDTO);

        // Act
        DocumentDetailsDTO result = documentService.createDocument(documentCreateDTO);

        // Assert
        verify(authorRepository, times(1)).findAllById(anyList());
        verify(documentRepository, times(1)).findAllById(anyList());
        verify(documentRepository, times(1)).save(any(Document.class));
        assertThat(result).isEqualTo(documentDetailsDTO);
    }

    @Test
    void shouldThrowWhenAuthorsNotFound() {
        // Arrange
        Author author1 = new Author(1L, "John", "Doe", new HashSet<>());
        Author author2 = new Author(2L, "Jane", "Doe", new HashSet<>());
        Set<Author> authorsFromClient = Set.of(author1, author2);
        List<Author> authorsFromRepo = List.of(author1);

        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", null, null);
        Document documentInput = new Document(null, "Doc 1", "Body 1", authorsFromClient, new HashSet<>(), null);

        when(documentMapper.mapToModel(documentCreateDTO)).thenReturn(documentInput);
        when(authorRepository.findAllById(anyList())).thenReturn(authorsFromRepo);

        // Act & Assert
        assertThatThrownBy(() -> documentService.createDocument(documentCreateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author")
                .hasMessageContaining("2");
    }

    @Test
    void shouldThrowWhenReferencesNotFound() {
        // Arrange
        Document doc1 = new Document(1L, "Doc 1", "Body 1", new HashSet<>(), null, null);
        Document doc2 = new Document(2L, "Doc 2", "Body 2", new HashSet<>(), null, null);
        Set<Document> documentsFromClient = Set.of(doc1, doc2);
        List<Document> documentsFromRepo = List.of(doc1);

        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", null, null);
        Document documentInput = new Document(null, "Doc 1", "Body 1", new HashSet<>(), documentsFromClient, null);

        when(documentMapper.mapToModel(documentCreateDTO)).thenReturn(documentInput);
        when(documentRepository.findAllById(anyList())).thenReturn(documentsFromRepo);

        // Act & Assert
        assertThatThrownBy(() -> documentService.createDocument(documentCreateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document")
                .hasMessageContaining("2");
    }

    @Test
    void shouldThrowWhenSelfReferencingDocument() {
        // Arrange
        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", null, null);
        Document documentInput = new Document(1L, "Doc 1", "Body 1", null, Set.of(new Document(1L, "Doc 1", "Body 1", null, null, null)), null);

        when(documentMapper.mapToModel(documentCreateDTO)).thenReturn(documentInput);

        // Act & Assert
        assertThatThrownBy(() -> documentService.createDocument(documentCreateDTO))
                .isInstanceOf(AssociationConflictException.class)
                .hasMessageContaining("Document can not reference itself");
    }
}