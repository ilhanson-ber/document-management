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
import com.ilhanson.document_management.services.helpers.AssociationUtils;
import com.ilhanson.document_management.services.helpers.CollectionUtils;
import com.ilhanson.document_management.services.helpers.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return saveAuthor(authorInput, author);
    }

    @Transactional
    public AuthorDetailsDTO updateAuthor(AuthorUpdateDTO authorDTOInput) {
        Author authorInput = authorMapper.mapToModel(authorDTOInput);
        Author author = getAuthorById(authorInput.getId());

        return saveAuthor(authorInput, author);
    }

    private AuthorDetailsDTO saveAuthor(Author input, Author output) {
        output.setFirstName(input.getFirstName());
        output.setLastName(input.getLastName());
        if (AssociationUtils.shouldUpdateAssociations(input.getDocuments(), output.getDocuments()))
            associateDocumentsWithAuthor(input.getDocuments(), output);

        Author savedAuthor = authorRepository.save(output);
        return authorMapper.mapToDetailsDTO(savedAuthor);
    }

    private void associateDocumentsWithAuthor(Set<Document> documentsFromClient, Author author) {
        List<Document> documentsFromRepo = documentRepository.findAllById(
                CollectionUtils.extractIds(documentsFromClient));

        validateRequestedDocumentsExistsOrThrow(documentsFromRepo, documentsFromClient);

        AssociationUtils.associateEntitiesWithOwner(
                documentsFromClient,
                documentsFromRepo,
                author.getDocuments(),
                author,
                Author::addDocument,
                Author::removeDocument
        );
    }

    private void validateRequestedDocumentsExistsOrThrow(List<Document> documentsFromRepo, Set<Document> documentsFromClient) {
        ValidationUtils.validateEntitiesExistOrThrow(
                documentsFromRepo,
                documentsFromClient,
                "Document(s)"
        );
    }

    private Author getAuthorById(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author", id));
    }
}
