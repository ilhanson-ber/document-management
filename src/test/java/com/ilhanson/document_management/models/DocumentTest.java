package com.ilhanson.document_management.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class DocumentTest {

  @Test
  void testAddReference() {
    Document document =
        new Document(1L, "Doc Title", "Doc", new HashSet<>(), new HashSet<>(), new HashSet<>());
    Document reference =
        new Document(2L, "Ref Title", "Ref", new HashSet<>(), new HashSet<>(), new HashSet<>());

    document.addReference(reference);

    assertThat(document.getReferences()).contains(reference);
    assertThat(reference.getReferredBy()).contains(document);
  }

  @Test
  void testRemoveReference() {
    Document document =
        new Document(1L, "Doc Title", "Doc", new HashSet<>(), new HashSet<>(), new HashSet<>());
    Document reference =
        new Document(2L, "Ref Title", "Ref", new HashSet<>(), new HashSet<>(), new HashSet<>());

    document.addReference(reference);
    document.removeReference(reference);

    assertThat(document.getReferences()).doesNotContain(reference);
    assertThat(reference.getReferredBy()).doesNotContain(document);
  }

  // entities with the same ids should be evaluated as equal although
  // the other fields might differ
  @Test
  void testEqualsAndHashCode() {
    Document document1 =
        new Document(1L, "Doc Title", "Doc", new HashSet<>(), new HashSet<>(), new HashSet<>());
    Document document2 =
        new Document(1L, "Ref Title", "Ref", new HashSet<>(), new HashSet<>(), new HashSet<>());

    assertThat(document1).isEqualTo(document2);
    assertThat(document1.hashCode()).isEqualTo(document2.hashCode());
  }
}
