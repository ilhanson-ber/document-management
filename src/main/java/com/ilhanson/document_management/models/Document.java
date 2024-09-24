package com.ilhanson.document_management.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @ManyToMany(mappedBy = "documents")
    // shouldn't be final so that mappers work correctly
    private Set<Author> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "document_reference",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "reference_id")
    )
    // shouldn't be final so that mappers work correctly
    private Set<Document> references = new HashSet<>();

    @ManyToMany(mappedBy = "references")
    // shouldn't be final so that mappers work correctly
    private Set<Document> referredBy = new HashSet<>();

    // As the owner of the relationship
    // providing the utility method to keep both sides in sync
    public void addReference(Document document) {
        this.references.add(document);
        document.getReferredBy().add(this);
    }

    // As the owner of the relationship
    // providing the utility method to keep both sides in sync
    public void removeReference(Document document) {
        this.references.remove(document);
        document.getReferredBy().remove(this);
    }

    // Removes the data associations where the document
    // is not the owner of the relationship.
    // For owners, JPA takes care of it while removal
    // For referencing ones (using mappedBy), we need to do it manually before removal
    @PreRemove
    private void prepareForRemoval() {
        this.authors.forEach(author -> author.getDocuments().remove(this));
        this.authors.clear();

        this.referredBy.forEach(document -> document.getReferences().remove(this));
        this.referredBy.clear();
    }
}
