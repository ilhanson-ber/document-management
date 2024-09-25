package com.ilhanson.document_management.services.helpers;

import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AssociationUtilsTest {

    @Test
    void shouldUpdateAssociationsWhenSetsAreDifferent() {
        Set<Document> current = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        Set<Document> requested = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(3L, "Doc 3", "Body 3", new HashSet<>(), new HashSet<>(), new HashSet<>()));

        boolean shouldUpdate = AssociationUtils.shouldUpdateAssociations(requested, current);

        assertThat(shouldUpdate).isTrue();
    }

    @Test
    void shouldNotUpdateAssociationsWhenSetsAreEqual() {
        Set<Document> current = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        Set<Document> requested = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));

        boolean shouldUpdate = AssociationUtils.shouldUpdateAssociations(requested, current);

        assertThat(shouldUpdate).isFalse();
    }

    @Test
    void shouldNotUpdateAssociationsWhenSetsAreEmpty() {
        Set<Document> current = new HashSet<>();
        Set<Document> requested = new HashSet<>();

        boolean shouldUpdate = AssociationUtils.shouldUpdateAssociations(requested, current);

        assertThat(shouldUpdate).isFalse();
    }

    @Test
    void shouldAddAndRemoveDocumentsFromAuthor() {
        Set<Document> documentsFromClient = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        List<Document> documentsFromRepo = List.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        Set<Document> documentsFromAuthor = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(3L, "Doc 3", "Body 3", new HashSet<>(), new HashSet<>(), new HashSet<>()));

        Author author = new Author(1L, "John", "Doe", new HashSet<>(documentsFromAuthor));

        BiConsumer<Author, Document> addDocumentToAuthor = spy(new BiConsumer<>() {
            @Override
            public void accept(Author auth, Document doc) {
                auth.addDocument(doc);
            }
        });
        BiConsumer<Author, Document> removeDocumentFromAuthor = spy(new BiConsumer<>() {
            @Override
            public void accept(Author auth, Document doc) {
                auth.removeDocument(doc);
            }
        });

        // Perform association updates
        AssociationUtils.associateEntitiesWithOwner(
                documentsFromClient,
                documentsFromRepo,
                author.getDocuments(),
                author,
                addDocumentToAuthor,
                removeDocumentFromAuthor
        );

        // Verify documents added and removed
        verify(addDocumentToAuthor, times(1)).accept(author, new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        verify(removeDocumentFromAuthor, times(1)).accept(author, new Document(3L, "Doc 3", "Body 3", new HashSet<>(), new HashSet<>(), new HashSet<>()));

        // Verify final state
        assertThat(author.getDocuments()).containsExactlyInAnyOrder(
                new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>())
        );
    }

    @Test
    void shouldNotAddOrRemoveDocumentsWhenSetsAreEqual() {
        Set<Document> documentsFromClient = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        List<Document> documentsFromRepo = List.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        Set<Document> documentsFromAuthor = Set.of(new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>()));

        Author author = new Author(1L, "John", "Doe", new HashSet<>(documentsFromAuthor));

        BiConsumer<Author, Document> addDocumentToAuthor = spy(new BiConsumer<>() {
            @Override
            public void accept(Author auth, Document doc) {
                auth.addDocument(doc);
            }
        });
        BiConsumer<Author, Document> removeDocumentFromAuthor = spy(new BiConsumer<>() {
            @Override
            public void accept(Author auth, Document doc) {
                auth.removeDocument(doc);
            }
        });

        // Perform association updates
        AssociationUtils.associateEntitiesWithOwner(
                documentsFromClient,
                documentsFromRepo,
                author.getDocuments(),
                author,
                addDocumentToAuthor,
                removeDocumentFromAuthor
        );

        // Verify no documents added or removed
        verify(addDocumentToAuthor, never()).accept(any(), any());
        verify(removeDocumentFromAuthor, never()).accept(any(), any());

        // Verify final state
        assertThat(author.getDocuments()).containsExactlyInAnyOrder(
                new Document(1L, "Doc 1", "Body 1", new HashSet<>(), new HashSet<>(), new HashSet<>()),
                new Document(2L, "Doc 2", "Body 2", new HashSet<>(), new HashSet<>(), new HashSet<>())
        );
    }
}
