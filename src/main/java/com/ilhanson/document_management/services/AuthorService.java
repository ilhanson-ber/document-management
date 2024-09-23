package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.mappers.AuthorMapper;
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
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final DocumentRepository documentRepository;

    private final AuthorMapper authorMapper;

    private final DocumentMapper documentMapper;


    @Transactional(readOnly = true)
    public AuthorDetailsDTO getAuthorDetails(Long id) {
        Author author = getAuthorById(id);
        return authorMapper.mapToDetailsDTO(author);
    }

    @Transactional(readOnly = true)
    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll().stream().map(authorMapper::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAuthor(Long id) {
        Author author = getAuthorById(id);
        authorRepository.delete(author);
    }

    @Transactional
    public AuthorDetailsDTO createAuthor(AuthorCreateDTO authorInput) {
        Author author = authorMapper.mapToModel(authorInput);

        List<Long> requestedDocumentIds = getIdsFromUserInput(authorInput.getDocuments());
        if (requestedDocumentIds != null && !requestedDocumentIds.isEmpty()) {
            associateDocumentsWithAuthor(requestedDocumentIds, author.getDocuments(), author);
        }

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    @Transactional
    public AuthorDetailsDTO updateAuthor(AuthorUpdateDTO authorInput) {
        Author author = authorMapper.mapToModel(authorInput);
        Set<Document> existingDocuments = getAuthorById(author.getId()).getDocuments();

        List<Long> requestedDocumentIds = getIdsFromUserInput(authorInput.getDocuments());
        associateDocumentsWithAuthor(requestedDocumentIds, existingDocuments, author);

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    private void associateDocumentsWithAuthor(List<Long> requestedDocumentIds, Set<Document> existingDocuments, Author author) {
        List<Document> requestedDocuments = documentRepository.findAllById(requestedDocumentIds);
        validateRequestedDocumentsExists(requestedDocuments, requestedDocumentIds);

        Set<Long> requestedDocumentsIdSet = new HashSet<>(requestedDocumentIds);
        Set<Long> existingDocumentsIdSet = existingDocuments.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        Set<Long> documentsToAddIds = new HashSet<>(requestedDocumentsIdSet);
        documentsToAddIds.removeAll(existingDocumentsIdSet);

        Set<Long> documentsToRemoveIds = new HashSet<>(existingDocumentsIdSet);
        documentsToRemoveIds.removeAll(requestedDocumentsIdSet);

        // Add new documents to the author
        for (Document document : requestedDocuments) {
            if (documentsToAddIds.contains(document.getId())) {
                author.addDocument(document);
            }
        }

        // Remove documents from the author
        for (Document document : existingDocuments) {
            if (documentsToRemoveIds.contains(document.getId())) {
                author.removeDocument(document);
            }
        }
    }

    private void validateRequestedDocumentsExists(List<Document> documents, List<Long> requestedIds) {
        Set<Long> foundIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Document(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private Author getAuthorById(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author with ID " + id + "do not exist"));
    }

    private List<Long> getIdsFromUserInput(List<IdInputDTO> inputList) {
        return inputList.stream()
                .map(IdInputDTO::getId)
                .collect(Collectors.toList());
    }
}
