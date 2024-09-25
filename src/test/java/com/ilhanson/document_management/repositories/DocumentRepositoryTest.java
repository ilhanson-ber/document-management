package com.ilhanson.document_management.repositories;

import com.ilhanson.document_management.models.Document;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindAllDocuments() {
        var documents = documentRepository.findAll();
        assertThat(documents).hasSize(5);
    }

    @Test
    void shouldFindDocumentById() {
        Optional<Document> document = documentRepository.findById(1L);
        assertThat(document).isPresent();
        assertThat(document.get().getTitle()).isEqualTo("Document 1");
    }

    @Test
    void shouldFindReferencesForDocument() {
        Document document = documentRepository.findById(1L).orElseThrow();
        assertThat(document.getReferences()).hasSize(3);
    }

    @Test
    void shouldFindAuthorsForDocument() {
        Document document = documentRepository.findById(1L).orElseThrow();
        assertThat(document.getAuthors()).hasSize(2);
    }

    @Test
    void shouldFindDocumentsReferredByOtherDocuments() {
        Document document = documentRepository.findById(1L).orElseThrow();
        assertThat(document.getReferredBy()).hasSize(2);
    }

    @Test
    void shouldCreateNewDocument() {
        Document document = Document.builder()
                .title("New Document")
                .body("This is a new document body.")
                .authors(new HashSet<>())
                .references(new HashSet<>())
                .referredBy(new HashSet<>())
                .build();

        Document savedDocument = documentRepository.save(document);

        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getTitle()).isEqualTo("New Document");
    }


    @Test
    void shouldDeleteDocumentAndRemoveAssociations() {
        Document document = documentRepository.findById(1L).orElseThrow();
        authorRepository.findById(1L).ifPresent(author -> {
            assertThat(author.getDocuments()).contains(document);
        });

        documentRepository.delete(document);
        documentRepository.flush();
        entityManager.clear();

        Optional<Document> deletedDocument = documentRepository.findById(1L);
        assertThat(deletedDocument).isEmpty();

        authorRepository.findById(1L).ifPresent(author -> {
            assertThat(author.getDocuments()).doesNotContain(document);
        });
    }

    @Test
    void shouldNotCreateDocumentWithEmptyTitle() {
        Document document = Document.builder()
                .title("")
                .body("Document body.")
                .authors(new HashSet<>())
                .references(new HashSet<>())
                .build();

        try {
            documentRepository.save(document);
            assertThat(false).isTrue(); // Should not reach this line
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Test
    void shouldNotCreateDocumentWithEmptyBody() {
        Document document = Document.builder()
                .title("Valid Title")
                .body("")
                .authors(new HashSet<>())
                .references(new HashSet<>())
                .build();

        try {
            documentRepository.save(document);
            assertThat(false).isTrue(); // Should not reach this line
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Test
    void shouldAddReferenceToDocument() {
        Document document = documentRepository.findById(1L).orElseThrow();
        Document reference = Document.builder()
                .title("Reference Document")
                .body("This is a reference document.")
                .authors(new HashSet<>())
                .references(new HashSet<>())
                .referredBy(new HashSet<>())
                .build();

        documentRepository.save(reference);
        document.addReference(reference);
        documentRepository.save(document);
        documentRepository.flush();
        entityManager.clear();

        documentRepository.findById(1L).ifPresent(doc -> {
            assertThat(doc.getReferences()).contains(reference);
        });
        documentRepository.findById(reference.getId()).ifPresent(ref -> {
            assertThat(ref.getReferredBy()).contains(document);
        });
    }

    @Test
    void shouldRemoveReferenceFromDocument() {
        Document document = documentRepository.findById(1L).orElseThrow();
        Document reference = document.getReferences().iterator().next();

        document.removeReference(reference);
        documentRepository.save(document);
        documentRepository.flush();
        entityManager.clear();

        documentRepository.findById(1L).ifPresent(doc -> {
            assertThat(doc.getReferences()).doesNotContain(reference);
        });
        documentRepository.findById(reference.getId()).ifPresent(ref -> {
            assertThat(ref.getReferredBy()).doesNotContain(document);
        });
    }

    @Test
    void shouldDeleteDocumentAndRemoveReferenceAssociations() {
        Document document = documentRepository.findById(1L).orElseThrow();
        Document reference = document.getReferences().iterator().next();

        documentRepository.delete(document);
        documentRepository.flush();
        entityManager.clear();

        documentRepository.findById(reference.getId()).ifPresent(ref -> {
            assertThat(ref.getReferredBy()).doesNotContain(document);
        });
    }
}
