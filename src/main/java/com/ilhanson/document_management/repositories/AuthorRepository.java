package com.ilhanson.document_management.repositories;

import com.ilhanson.document_management.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
