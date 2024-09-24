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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

        document.setTitle(documentInput.getTitle());
        document.setBody(documentInput.getBody());
        associateAuthorsWithDocument(documentInput.getAuthors(), document);
        associateReferencesWithDocument(documentInput.getReferences(), document);

        Document savedDocument = documentRepository.save(document);
        return documentMapper.mapToDetailsDTO(savedDocument);
    }

    @Transactional
    public DocumentDetailsDTO updateDocument(DocumentUpdateDTO documentDTOInput) {
        Document documentInput = documentMapper.mapToModel(documentDTOInput);
        Document document = getDocumentById(documentInput.getId());

        document.setTitle(documentInput.getTitle());
        document.setBody(documentInput.getBody());
        associateAuthorsWithDocument(documentInput.getAuthors(), document);
        associateReferencesWithDocument(documentInput.getReferences(), document);

        Document savedDocument = documentRepository.save(document);
        return documentMapper.mapToDetailsDTO(savedDocument);
    }

    private void associateAuthorsWithDocument(Set<Author> authorsFromClient, Document document) {
        // we need to clone (freeze) the current state
        // because we will modify the state while we are
        // adding and removing associations
        // otherwise: ConcurrentModificationException
        Set<Author> authorsBeforeRequest = document.getAuthors();

        if (authorsFromClient == null) authorsFromClient = new HashSet<>();

        // associations did not change, exit from here
        if (authorsFromClient.equals(authorsBeforeRequest)) return;

        List<Author> authorsFromRepo = authorRepository.findAllById(
                authorsFromClient.stream()
                        .map(Author::getId)
                        .collect(Collectors.toList()));

        validateRequestedAuthorsExistsOrThrow(authorsFromRepo, authorsFromClient);

        Set<Author> authorsToAdd = new HashSet<>(authorsFromClient);
        authorsToAdd.removeAll(authorsBeforeRequest);

        Set<Author> authorsToRemove = new HashSet<>(authorsBeforeRequest);
        authorsToRemove.removeAll(authorsFromClient);

        // because authors are the owner of the author - document relationship
        // we need to update the author list for the current document
        // by adding or removing the document from authors

        // add new documents to the author
        for (Author author : authorsFromRepo) {
            if (authorsToAdd.contains(author)) {
                author.addDocument(document);
            }
        }

        for (Author author : authorsBeforeRequest) {
            if (authorsToRemove.contains(author)) {
                author.removeDocument(document);
            }
        }
    }

    private void associateReferencesWithDocument(Set<Document> referencesFromClient, Document document) {
        // we need to clone (freeze) the current state
        // because we will modify the state while we are
        // adding and removing associations
        // otherwise: ConcurrentModificationException
        Set<Document> referencesBeforeRequest = document.getReferences();

        if (referencesFromClient == null) referencesFromClient = new HashSet<>();

        // associations did not change, exit from here
        if (referencesFromClient.equals(referencesBeforeRequest)) return;

        List<Document> referencesFromRepo = documentRepository.findAllById(
                referencesFromClient.stream()
                        .map(Document::getId)
                        .collect(Collectors.toList()));

        validateRequestedReferencesExistsOrThrow(referencesFromRepo, referencesFromClient);
        validateNoSelfReferencingOrThrow(referencesFromClient, document);


        Set<Document> referencesToAdd = new HashSet<>(referencesFromClient);
        referencesToAdd.removeAll(referencesBeforeRequest);

        Set<Document> referencesToRemove = new HashSet<>(referencesBeforeRequest);
        referencesToRemove.removeAll(referencesFromClient);

        // add new references to the document
        for (Document reference : referencesFromRepo) {
            if (referencesToAdd.contains(reference)) {
                document.addReference(reference);
            }
        }

        // remove references from the document
        for (Document reference : referencesBeforeRequest) {
            if (referencesToRemove.contains(reference)) {
                document.removeReference(reference);
            }
        }
    }

    private void validateRequestedAuthorsExistsOrThrow(List<Author> authorsFromRepo, Set<Author> authorsFromClient) {
        Set<Long> authorIdsFromRepo = authorsFromRepo.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = authorsFromClient.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());

        missingIds.removeAll(authorIdsFromRepo);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Author(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private void validateNoSelfReferencingOrThrow(Set<Document> referencesFromClient, Document document) {
        if (referencesFromClient.contains(document)) {
            throw new AssociationConflictException("Document can not reference itself. Remove ID(" + document.getId() + ") from the reference list");
        }
    }

    private void validateRequestedReferencesExistsOrThrow(List<Document> referencesFromRepo, Set<Document> referencesFromClient) {
        Set<Long> referenceIdsFromRepo = referencesFromRepo.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = referencesFromClient.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        missingIds.removeAll(referenceIdsFromRepo);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Document(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document with ID " + id + " does not exist"));
    }
}
