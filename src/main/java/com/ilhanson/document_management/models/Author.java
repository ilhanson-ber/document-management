package com.ilhanson.document_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "author")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// We need to provide customized equals and hashCode methods
// because we use equality checks when operating on a Set<>
// to manage the entity associations
// WARNING: Lombok's @EqualsAndHashCode treats two objects
// with null value as the same. This might be problematic
// if we ever want to add two null id objects to the Set
// at the same time - potentially making use of
// automatic persist and merge cascading.
// In that case, replace these methods with a version supporting
// a better comparison for null id objects
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Author implements Identifiable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Size(min = 1)
  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Size(min = 1)
  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @ManyToMany
  @JoinTable(
      name = "author_document",
      joinColumns = @JoinColumn(name = "author_id"),
      inverseJoinColumns = @JoinColumn(name = "document_id"))
  // shouldn't be final so that mappers work correctly
  private Set<Document> documents = new HashSet<>();

  // As the owner of the relationship
  // providing the utility method to keep both sides in sync
  public void addDocument(Document document) {
    this.documents.add(document);
    document.getAuthors().add(this);
  }

  // As the owner of the relationship
  // providing the utility method to keep both sides in sync
  public void removeDocument(Document document) {
    this.documents.remove(document);
    document.getAuthors().remove(this);
  }
}
