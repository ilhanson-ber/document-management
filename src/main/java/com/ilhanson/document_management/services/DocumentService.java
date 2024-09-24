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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DocumentService {
    private final AuthorRepository authorRepository;
    private final DocumentRepository documentRepository;

    private final DocumentMapper documentMapper;

    @Transactional(readOnly = true)
    public DocumentDetailsDTO getDocumentDetails(Long id) {
        Document document = getDocumentById(id);
        return documentMapper.mapToDetailsDTO(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream().map(documentMapper::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);
        documentRepository.delete(document);
    }

    @Transactional
    public DocumentDetailsDTO createDocument(DocumentCreateDTO documentDTOInput) {
        Document documentInput = documentMapper.mapToModel(documentDTOInput);
        Document document = new Document();

        return saveDocument(documentInput, document);
    }

    @Transactional
    public DocumentDetailsDTO updateDocument(DocumentUpdateDTO documentDTOInput) {
        Document documentInput = documentMapper.mapToModel(documentDTOInput);
        Document document = getDocumentById(documentInput.getId());

        return saveDocument(documentInput, document);
    }

    private DocumentDetailsDTO saveDocument(Document input, Document output) {
        output.setTitle(input.getTitle());
        output.setBody(input.getBody());
        if (AssociationUtils.shouldUpdateAssociations(input.getAuthors(), output.getAuthors()))
            associateAuthorsWithDocument(input.getAuthors(), output);
        if (AssociationUtils.shouldUpdateAssociations(input.getReferences(), output.getReferences()))
            associateReferencesWithDocument(input.getReferences(), output);

        Document savedDocument = documentRepository.save(output);
        return documentMapper.mapToDetailsDTO(savedDocument);
    }

    private void associateAuthorsWithDocument(Set<Author> authorsFromClient, Document document) {
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
            throw new AssociationConflictException(MessageFormat.format("Document can not reference itself. Remove ID({0}) from the reference list", document.getId()));
        }
    }

    private void validateRequestedAuthorsExistsOrThrow(List<Author> authorsFromRepo, Set<Author> authorsFromClient) {
        ValidationUtils.validateEntitiesExistOrThrow(
                authorsFromRepo,
                authorsFromClient,
                "Author(s)"
        );
    }

    private void validateRequestedReferencesExistsOrThrow(List<Document> referencesFromRepo, Set<Document> referencesFromClient) {
        ValidationUtils.validateEntitiesExistOrThrow(
                referencesFromRepo,
                referencesFromClient,
                "Document(s)"
        );
    }

    private Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }
}
