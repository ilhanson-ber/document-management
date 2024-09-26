package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.DocumentCreateDTO;
import com.ilhanson.document_management.dtos.DocumentDTO;
import com.ilhanson.document_management.dtos.DocumentDetailsDTO;
import com.ilhanson.document_management.dtos.DocumentUpdateDTO;
import com.ilhanson.document_management.exceptions.AssociationConflictException;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.mappers.DocumentMapper;
import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import com.ilhanson.document_management.repositories.AuthorRepository;
import com.ilhanson.document_management.repositories.DocumentRepository;
import com.ilhanson.document_management.services.helpers.AssociationUtils;
import com.ilhanson.document_management.services.helpers.CollectionUtils;
import com.ilhanson.document_management.services.helpers.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentService {
    private final AuthorRepository authorRepository;
    private final DocumentRepository documentRepository;

    private final DocumentMapper documentMapper;

    @Transactional(readOnly = true)
    public DocumentDetailsDTO getDocumentDetails(Long id) {
        log.info("Fetching details for document with ID: {}", id);
        Document document = getDocumentById(id);
        log.debug("Document details retrieved: {}", document);
        return documentMapper.mapToDetailsDTO(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getAllDocuments() {
        log.info("Fetching all documents");
        List<DocumentDTO> documents = documentRepository.findAll().stream().map(documentMapper::mapToDTO).collect(Collectors.toList());
        log.debug("Total documents retrieved: {}", documents.size());
        return documents;
    }

    @Transactional
    public void deleteDocument(Long id) {
        log.info("Deleting document with ID: {}", id);
        Document document = getDocumentById(id);
        documentRepository.delete(document);
        log.info("Document with ID: {} deleted successfully", id);
    }

    @Transactional
    public DocumentDetailsDTO createDocument(DocumentCreateDTO documentDTOInput) {
        log.info("Creating new document: {}", documentDTOInput.getTitle());
        Document documentInput = documentMapper.mapToModel(documentDTOInput);
        Document document = new Document();
        DocumentDetailsDTO createdDocument = saveDocument(documentInput, document);
        log.info("Document created successfully with ID: {}", createdDocument.getId());
        return createdDocument;
    }

    @Transactional
    public DocumentDetailsDTO updateDocument(DocumentUpdateDTO documentDTOInput) {
        log.info("Updating document with ID: {}", documentDTOInput.getId());
        Document documentInput = documentMapper.mapToModel(documentDTOInput);
        Document document = getDocumentById(documentInput.getId());
        DocumentDetailsDTO updatedDocument = saveDocument(documentInput, document);
        log.info("Document with ID: {} updated successfully", updatedDocument.getId());
        return updatedDocument;
    }

    private DocumentDetailsDTO saveDocument(Document input, Document output) {
        log.debug("Saving document: {}", input.getTitle());
        output.setTitle(input.getTitle());
        output.setBody(input.getBody());

        if (AssociationUtils.shouldUpdateAssociations(input.getAuthors(), output.getAuthors())) {
            associateAuthorsWithDocument(input.getAuthors(), output);
        }

        if (AssociationUtils.shouldUpdateAssociations(input.getReferences(), output.getReferences())) {
            associateReferencesWithDocument(input.getReferences(), output);
        }

        Document savedDocument = documentRepository.save(output);
        log.debug("Document saved with ID: {}", savedDocument.getId());
        return documentMapper.mapToDetailsDTO(savedDocument);
    }

    private void associateAuthorsWithDocument(Set<Author> authorsFromClient, Document document) {
        log.debug("Associating authors {} with document: {}", authorsFromClient, document.getId());
        List<Author> authorsFromRepo = authorRepository.findAllById(
                CollectionUtils.extractIds(authorsFromClient));

        validateRequestedAuthorsExistsOrThrow(authorsFromRepo, authorsFromClient);

        AssociationUtils.associateEntitiesWithOwner(
                authorsFromClient,
                authorsFromRepo,
                document.getAuthors(),
                document,
                (doc, author) -> author.addDocument(doc),
                (doc, author) -> author.removeDocument(doc)
        );
    }

    private void associateReferencesWithDocument(Set<Document> referencesFromClient, Document document) {
        log.debug("Associating references {} with document: {}", referencesFromClient, document.getId());
        List<Document> referencesFromRepo = documentRepository.findAllById(
                CollectionUtils.extractIds(referencesFromClient));

        validateRequestedReferencesExistsOrThrow(referencesFromRepo, referencesFromClient);
        validateNoSelfReferencingOrThrow(referencesFromClient, document);

        AssociationUtils.associateEntitiesWithOwner(
                referencesFromClient,
                referencesFromRepo,
                document.getReferences(),
                document,
                Document::addReference,
                Document::removeReference
        );
    }

    private void validateNoSelfReferencingOrThrow(Set<Document> referencesFromClient, Document document) {
        if (referencesFromClient.contains(document)) {
            log.error("Document with ID: {} cannot reference itself", document.getId());
            throw new AssociationConflictException(MessageFormat.format("Document can not reference itself. Remove ID({0}) from the reference list", document.getId()));
        }
    }

    private void validateRequestedAuthorsExistsOrThrow(List<Author> authorsFromRepo, Set<Author> authorsFromClient) {
        log.debug("Validating author existence for document");
        ValidationUtils.validateEntitiesExistOrThrow(
                authorsFromRepo,
                authorsFromClient,
                "Author(s)"
        );
    }

    private void validateRequestedReferencesExistsOrThrow(List<Document> referencesFromRepo, Set<Document> referencesFromClient) {
        log.debug("Validating reference existence for document");
        ValidationUtils.validateEntitiesExistOrThrow(
                referencesFromRepo,
                referencesFromClient,
                "Document(s)"
        );
    }

    private Document getDocumentById(Long id) {
        log.debug("Fetching document by ID: {}", id);
        return documentRepository.findById(id).orElseThrow(() -> {
            log.error("Document not found with ID: {}", id);
            return new ResourceNotFoundException("Document", id);
        });
    }
}
