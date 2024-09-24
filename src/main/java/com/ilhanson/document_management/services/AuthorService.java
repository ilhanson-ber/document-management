package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.AuthorCreateDTO;
import com.ilhanson.document_management.dtos.AuthorDTO;
import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.dtos.AuthorUpdateDTO;
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
        Author authorInput = authorMapper.mapToModel(authorDTOInput);
        Author author = new Author();

        author.setFirstName(authorInput.getFirstName());
        author.setLastName(authorInput.getLastName());
        associateDocumentsWithAuthor(authorInput.getDocuments(), author);

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    @Transactional
    public AuthorDetailsDTO updateAuthor(AuthorUpdateDTO authorDTOInput) {
        Author authorInput = authorMapper.mapToModel(authorDTOInput);
        Author author = getAuthorById(authorInput.getId());

        author.setFirstName(authorInput.getFirstName());
        author.setLastName(authorInput.getLastName());
        associateDocumentsWithAuthor(authorInput.getDocuments(), author);

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    private void associateDocumentsWithAuthor(Set<Document> documentsFromClient, Author author) {
        // we need to clone (freeze) the current state
        // because we will modify the state while we are
        // adding and removing associations
        // otherwise: ConcurrentModificationException
        Set<Document> documentsBeforeRequest = author.getDocuments();

        if (documentsFromClient == null) documentsFromClient = new HashSet<>();

        // associations did not change, exit from here
        if (documentsFromClient.equals(documentsBeforeRequest)) return;

        List<Document> documentsFromRepo = documentRepository.findAllById(
                documentsFromClient.stream()
                        .map(Document::getId)
                        .collect(Collectors.toList()));

        validateRequestedDocumentsExistsOrThrow(documentsFromRepo, documentsFromClient);

        Set<Document> documentsToAdd = new HashSet<>(documentsFromClient);
        documentsToAdd.removeAll(documentsBeforeRequest);

        Set<Document> documentsToRemove = new HashSet<>(documentsBeforeRequest);
        documentsToRemove.removeAll(documentsFromClient);

        // add new documents to the author
        for (Document document : documentsFromRepo) {
            if (documentsToAdd.contains(document)) {
                author.addDocument(document);
            }
        }

        // remove documents from the author
        for (Document document : documentsBeforeRequest) {
            if (documentsToRemove.contains(document)) {
                author.removeDocument(document);
            }
        }
    }

    private void validateRequestedDocumentsExistsOrThrow(List<Document> documentsFromRepo, Set<Document> documentsFromClient) {
        Set<Long> documentIdsFromRepo = documentsFromRepo.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = documentsFromClient.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        missingIds.removeAll(documentIdsFromRepo);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Document(s) with ID(s) " + missingIds + " do not exist");
        }
    }

    private Author getAuthorById(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author with ID " + id + " does not exist"));
    }
}
