package com.ilhanson.document_management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties("documents")
    private final Set<Author> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "document_reference",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "reference_id")
    )
    @JsonIgnoreProperties({"references", "referredBy", "authors"})
    private final Set<Document> references = new HashSet<>();

    @ManyToMany(mappedBy = "references")
    @JsonIgnoreProperties({"references", "referredBy", "authors"})
    private final Set<Document> referredBy = new HashSet<>();

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

    // Removes the associations where the document
    // is not the owner of the relationship
    // For owners, JPA takes care of it while removal
    // For referencing ones, we need to do it manually before removal
    public void prepareForRemoval() {
        this.authors.forEach(author -> author.getDocuments().remove(this));
        this.authors.clear();

        this.referredBy.forEach(document -> document.getReferences().remove(this));
        this.referredBy.clear();
    }
}
