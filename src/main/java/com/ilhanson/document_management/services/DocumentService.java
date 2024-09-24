package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.*;
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
import java.util.Map;
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
        Document document = documentMapper.mapToModel(documentDTOInput);

        List<IdInputDTO> requestedAuthorIds = documentDTOInput.getAuthors();
        // we need a null check because author list is not
        // mandatory in document create request
        // (in contrast to update request)
        if (requestedAuthorIds != null && !requestedAuthorIds.isEmpty()) {
            associateAuthorsWithDocument(getIdsFromUserInput(requestedAuthorIds), document);
        }

        List<IdInputDTO> requestedReferenceIds = documentDTOInput.getReferences();
        if (requestedReferenceIds != null && !requestedReferenceIds.isEmpty()) {
            associateReferencesWithDocument(getIdsFromUserInput(requestedReferenceIds), document);
        }

        Document savedDocument = documentRepository.save(document);
        return documentMapper.mapToDetailsDTO(savedDocument);
    }

    @Transactional
    public DocumentDetailsDTO updateDocument(DocumentUpdateDTO documentDTOInput) {
        Document documentInput = documentMapper.mapToModel(documentDTOInput);
        Document document = getDocumentById(documentInput.getId());

        document.setTitle(documentInput.getTitle());
        document.setBody(documentInput.getBody());

        List<Long> requestedAuthorIds = getIdsFromUserInput(documentDTOInput.getAuthors());
        associateAuthorsWithDocument(requestedAuthorIds, document);

        List<Long> requestedReferenceIds = getIdsFromUserInput(documentDTOInput.getReferences());
        associateReferencesWithDocument(requestedReferenceIds, document);

        Document savedDocument = documentRepository.save(document);
        return documentMapper.mapToDetailsDTO(savedDocument);
    }

    private void associateAuthorsWithDocument(List<Long> requestedAuthorIds, Document document) {
        List<Author> requestedAuthors = authorRepository.findAllById(requestedAuthorIds);
        validateRequestedAuthorsExistsOrThrow(requestedAuthors, requestedAuthorIds);

        Set<Author> beforeRequestAuthors = new HashSet<>(document.getAuthors());

        Set<Long> requestedAuthorsIdSet = new HashSet<>(requestedAuthorIds);
        Set<Long> beforeRequestAuthorsIdSet = beforeRequestAuthors.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());

        // author list did not change, exit from here
        if (requestedAuthorsIdSet.equals(beforeRequestAuthorsIdSet)) return;

        Set<Long> authorIdsToAdd = new HashSet<>(requestedAuthorsIdSet);
        authorIdsToAdd.removeAll(beforeRequestAuthorsIdSet);

        Set<Long> authorIdsToRemove = new HashSet<>(beforeRequestAuthorsIdSet);
        authorIdsToRemove.removeAll(requestedAuthorsIdSet);

        // will be used to quick lookup for authors
        Map<Long, Author> authorMap = requestedAuthors.stream()
                .collect(Collectors.toMap(Author::getId, author -> author));

        // because authors are the owner of the author - document relationship
        // we need to update the author list for the current document
        // by adding or removing the document from authors

        for (Long id : authorIdsToAdd) {
            authorMap.get(id).addDocument(document);
        }

        for (Long id : authorIdsToRemove) {
            authorMap.get(id).removeDocument(document);
        }
    }

    private void associateReferencesWithDocument(List<Long> requestedReferenceIds, Document document) {
        List<Document> requestedDocuments = documentRepository.findAllById(requestedReferenceIds);
        validateRequestedReferencesExistsOrThrow(requestedDocuments, requestedReferenceIds);
        validateNoSelfReferencingOrThrow(requestedReferenceIds, document);

        Set<Document> beforeRequestReferences = new HashSet<>(document.getReferences());

        Set<Long> requestedReferencesIdSet = new HashSet<>(requestedReferenceIds);
        Set<Long> beforeRequestDocumentsIdSet = beforeRequestReferences.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        // document list did not change, exit from here
        if (requestedReferencesIdSet.equals(beforeRequestDocumentsIdSet)) return;

        Set<Long> documentIdsToAdd = new HashSet<>(requestedReferencesIdSet);
        documentIdsToAdd.removeAll(beforeRequestDocumentsIdSet);

        Set<Long> documentIdsToRemove = new HashSet<>(beforeRequestDocumentsIdSet);
        documentIdsToRemove.removeAll(requestedReferencesIdSet);

        // add new references to the document
        for (Document reference : requestedDocuments) {
            if (documentIdsToAdd.contains(reference.getId())) {
                document.addReference(reference);
            }
        }

        // remove references from the document
        for (Document reference : beforeRequestReferences) {
            if (documentIdsToRemove.contains(reference.getId())) {
                document.removeReference(reference);
            }
        }
    }

    private void validateRequestedAuthorsExistsOrThrow(List<Author> authors, List<Long> requestedIds) {
        Set<Long> foundIds = authors.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = new HashSet<>(requestedIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Author(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private void validateNoSelfReferencingOrThrow(List<Long> requestedReferenceIds, Document document) {
        if (requestedReferenceIds.contains(document.getId())) {
            throw new AssociationConflictException("Document can not reference itself. Remove ID(" + document.getId() + ") from the reference list");
        }
    }

    private void validateRequestedReferencesExistsOrThrow(List<Document> documents, List<Long> requestedIds) {
        Set<Long> foundIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = new HashSet<>(requestedIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Document(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document with ID " + id + " does not exist"));
    }

    private List<Long> getIdsFromUserInput(List<IdInputDTO> inputList) {
        return inputList.stream()
                .map(IdInputDTO::getId)
                .collect(Collectors.toList());
    }
}
