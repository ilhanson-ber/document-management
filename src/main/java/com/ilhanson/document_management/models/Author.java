package com.ilhanson.document_management.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "author")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @ManyToMany
    @JoinTable(
            name = "author_document",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    @JsonIgnoreProperties("authors")
    private final Set<Document> documents = new HashSet<>();

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
