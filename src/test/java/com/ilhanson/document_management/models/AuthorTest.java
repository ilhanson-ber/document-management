package com.ilhanson.document_management.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class AuthorTest {

  @Test
  void testAddDocument() {
    Author author = new Author();
    Document document = new Document();

    author.addDocument(document);

    assertThat(author.getDocuments()).contains(document);
    assertThat(document.getAuthors()).contains(author);
  }

  @Test
  void testRemoveDocument() {
    Author author = new Author();
    Document document = new Document();

    author.addDocument(document);
    author.removeDocument(document);

    assertThat(author.getDocuments()).doesNotContain(document);
    assertThat(document.getAuthors()).doesNotContain(author);
  }

  // same ids should be evaluated as the same entity although
  // the other fields might differ
  @Test
  void testEqualsAndHashCode() {
    Author author1 = new Author(1L, "A", "B", new HashSet<>());
    Author author2 = new Author(1L, "C", "D", new HashSet<>());

    assertThat(author1).isEqualTo(author2);
    assertThat(author1.hashCode()).isEqualTo(author2.hashCode());
  }
}
