package com.ilhanson.document_management.repositories;

import com.ilhanson.document_management.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {}
