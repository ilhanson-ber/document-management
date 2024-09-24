package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.mappers.AuthorMapper;
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
    public AuthorDetailsDTO createAuthor(AuthorCreateDTO authorDTOInput) {
        Author author = authorMapper.mapToModel(authorDTOInput);

        List<IdInputDTO> requestedDocumentIds = authorDTOInput.getDocuments();
        // we need a null check because document list is not
        // mandatory in author create request
        // (in contrast to update request)
        if (requestedDocumentIds != null && !requestedDocumentIds.isEmpty()) {
            associateDocumentsWithAuthor(getIdsFromUserInput(requestedDocumentIds), author);
        }

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    @Transactional
    public AuthorDetailsDTO updateAuthor(AuthorUpdateDTO authorDTOInput) {
        Author authorInput = authorMapper.mapToModel(authorDTOInput);
        Author author = getAuthorById(authorInput.getId());

        author.setFirstName(authorInput.getFirstName());
        author.setLastName(authorInput.getLastName());

        List<Long> requestedDocumentIds = getIdsFromUserInput(authorDTOInput.getDocuments());
        associateDocumentsWithAuthor(requestedDocumentIds, author);

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    private void associateDocumentsWithAuthor(List<Long> requestedDocumentIds, Author author) {
        List<Document> requestedDocuments = documentRepository.findAllById(requestedDocumentIds);
        validateRequestedDocumentsExistsOrThrow(requestedDocuments, requestedDocumentIds);

        Set<Document> beforeRequestDocuments = new HashSet<>(author.getDocuments());

        Set<Long> requestedDocumentsIdSet = new HashSet<>(requestedDocumentIds);
        Set<Long> existingDocumentsIdSet = beforeRequestDocuments.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        // document list did not change, exit from here
        if (requestedDocumentsIdSet.equals(existingDocumentsIdSet)) return;

        Set<Long> documentsToAddIds = new HashSet<>(requestedDocumentsIdSet);
        documentsToAddIds.removeAll(existingDocumentsIdSet);

        Set<Long> documentsToRemoveIds = new HashSet<>(existingDocumentsIdSet);
        documentsToRemoveIds.removeAll(requestedDocumentsIdSet);

        // add new documents to the author
        for (Document document : requestedDocuments) {
            if (documentsToAddIds.contains(document.getId())) {
                author.addDocument(document);
            }
        }

        // remove missing documents from the author
        for (Document document : beforeRequestDocuments) {
            if (documentsToRemoveIds.contains(document.getId())) {
                author.removeDocument(document);
            }
        }
    }

    private void validateRequestedDocumentsExistsOrThrow(List<Document> documents, List<Long> requestedIds) {
        Set<Long> foundIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = new HashSet<>(requestedIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Document(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private Author getAuthorById(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author with ID " + id + " does not exist"));
    }

    private List<Long> getIdsFromUserInput(List<IdInputDTO> inputList) {
        return inputList.stream()
                .map(IdInputDTO::getId)
                .collect(Collectors.toList());
    }
}
