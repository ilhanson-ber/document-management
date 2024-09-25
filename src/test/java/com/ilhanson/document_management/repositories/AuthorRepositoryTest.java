package com.ilhanson.document_management.repositories;

import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(3);
    }

    @Test
    void shouldFindAuthorById() {
        Optional<Author> author = authorRepository.findById(1L);
        assertThat(author).isPresent();
        assertThat(author.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindDocumentsByAuthor() {
        Author author = authorRepository.findById(1L).orElseThrow();
        assertThat(author.getDocuments()).hasSize(3);
    }

    @Test
    void shouldCreateNewAuthor() {
        Author author = Author.builder()
                .firstName("Alice")
                .lastName("Brown")
                .documents(new HashSet<>())
                .build();

        Author savedAuthor = authorRepository.save(author);

        assertThat(savedAuthor.getId()).isNotNull();
        assertThat(savedAuthor.getFirstName()).isEqualTo("Alice");
    }

    @Test
    void shouldAddDocumentToAuthor() {
        Author author = authorRepository.findById(1L).orElseThrow();
        Document document = Document.builder()
                .title("New Document")
                .body("This is a new document.")
                .authors(new HashSet<>())
                .build();

        documentRepository.save(document);
        author.addDocument(document);
        authorRepository.save(author);
        authorRepository.flush();
        entityManager.clear();

        authorRepository.findById(1L).ifPresent(author2 -> {
            assertThat(author2.getDocuments()).contains(document);
        });
        documentRepository.findById(document.getId()).ifPresent(document2 -> {
            assertThat(document2.getAuthors()).contains(author);
        });
    }

    @Test
    void shouldRemoveDocumentFromAuthor() {
        Author author = authorRepository.findById(1L).orElseThrow();
        Document document = author.getDocuments().iterator().next();

        author.removeDocument(document);
        authorRepository.save(author);
        authorRepository.flush();
        entityManager.clear();

        authorRepository.findById(1L).ifPresent(author2 -> {
            assertThat(author2.getDocuments()).doesNotContain(document);
        });
        documentRepository.findById(1L).ifPresent(document2 -> {
            assertThat(document2.getAuthors()).doesNotContain(author);
        });
    }

    @Test
    void shouldDeleteAuthorAndRemoveAssociations() {
        Author author = authorRepository.findById(1L).orElseThrow();
        documentRepository.findById(1L).ifPresent(document -> {
            assertThat(document.getAuthors()).contains(author);
        });

        authorRepository.delete(author);
        authorRepository.flush();
        entityManager.clear();

        Optional<Author> deletedAuthor = authorRepository.findById(1L);
        assertThat(deletedAuthor).isEmpty();

        documentRepository.findById(1L).ifPresent(document -> {
            assertThat(document.getAuthors()).doesNotContain(author);
        });
    }

    @Test
    void shouldNotCreateAuthorWithEmptyFirstName() {
        Author author = Author.builder()
                .firstName("")
                .lastName("Brown")
                .documents(new HashSet<>())
                .build();

        try {
            authorRepository.save(author);
            assertThat(false).isTrue(); // Should not reach this line
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Test
    void shouldNotCreateAuthorWithEmptyLastName() {
        Author author = Author.builder()
                .firstName("Alice")
                .lastName("")
                .documents(new HashSet<>())
                .build();

        try {
            authorRepository.save(author);
            assertThat(false).isTrue(); // Should not reach this line
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstraintViolationException.class);
        }
    }

}
