package com.ilhanson.document_management.services;

import com.ilhanson.document_management.dtos.AuthorCreateDTO;
import com.ilhanson.document_management.dtos.AuthorDTO;
import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.dtos.AuthorUpdateDTO;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.mappers.AuthorMapper;
import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import com.ilhanson.document_management.producers.AuthorEventProducer;
import com.ilhanson.document_management.repositories.AuthorRepository;
import com.ilhanson.document_management.repositories.DocumentRepository;
import com.ilhanson.document_management.services.helpers.AssociationUtils;
import com.ilhanson.document_management.services.helpers.CollectionUtils;
import com.ilhanson.document_management.services.helpers.ValidationUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthorService {
  private final AuthorRepository authorRepository;
  private final DocumentRepository documentRepository;

  private final AuthorEventProducer authorEventProducer;

  private final AuthorMapper authorMapper;

  @Transactional(readOnly = true)
  public AuthorDetailsDTO getAuthorDetails(Long id) {
    log.info("Fetching details for author with ID: {}", id);
    Author author = getAuthorById(id);
    log.debug("Author details retrieved: {}", author);
    return authorMapper.mapToDetailsDTO(author);
  }

  @Transactional(readOnly = true)
  public List<AuthorDTO> getAllAuthors() {
    log.info("Fetching all authors");
    List<AuthorDTO> authors =
        authorRepository.findAll().stream()
            .map(authorMapper::mapToDTO)
            .collect(Collectors.toList());
    log.debug("Total authors retrieved: {}", authors.size());
    return authors;
  }

  @Transactional
  public void deleteAuthor(Long id) {
    log.info("Deleting author with ID: {}", id);
    Author author = getAuthorById(id);
    authorRepository.delete(author);
    log.info("Author with ID: {} deleted successfully", id);

    authorEventProducer.sendAuthorDeletedEvent(id);
  }

  @Transactional
  public AuthorDetailsDTO createAuthor(AuthorCreateDTO authorDTOInput) {
    log.info(
        "Creating new author: {} {}", authorDTOInput.getFirstName(), authorDTOInput.getLastName());
    Author authorInput = authorMapper.mapToModel(authorDTOInput);
    Author author = new Author();
    AuthorDetailsDTO createdAuthor = saveAuthor(authorInput, author);
    log.info("Author created successfully with ID: {}", createdAuthor.getId());
    return createdAuthor;
  }

  @Transactional
  public AuthorDetailsDTO updateAuthor(AuthorUpdateDTO authorDTOInput) {
    log.info("Updating author with ID: {}", authorDTOInput.getId());
    Author authorInput = authorMapper.mapToModel(authorDTOInput);
    Author author = getAuthorById(authorInput.getId());
    AuthorDetailsDTO updatedAuthor = saveAuthor(authorInput, author);
    log.info("Author with ID: {} updated successfully", updatedAuthor.getId());

    authorEventProducer.sendAuthorUpdatedEvent(updatedAuthor);
    return updatedAuthor;
  }

  private AuthorDetailsDTO saveAuthor(Author input, Author output) {
    log.debug("Saving author: {} {}", input.getFirstName(), input.getLastName());
    output.setFirstName(input.getFirstName());
    output.setLastName(input.getLastName());
    if (AssociationUtils.shouldUpdateAssociations(input.getDocuments(), output.getDocuments())) {
      associateDocumentsWithAuthor(input.getDocuments(), output);
    }
    Author savedAuthor = authorRepository.save(output);
    log.debug("Author saved with ID: {}", savedAuthor.getId());
    return authorMapper.mapToDetailsDTO(savedAuthor);
  }

  private void associateDocumentsWithAuthor(Set<Document> documentsFromClient, Author author) {
    log.debug("Associating documents {} with author: {}", documentsFromClient, author.getId());
    List<Document> documentsFromRepo =
        documentRepository.findAllById(CollectionUtils.extractIds(documentsFromClient));
    validateRequestedDocumentsExistsOrThrow(documentsFromRepo, documentsFromClient);
    AssociationUtils.associateEntitiesWithOwner(
        documentsFromClient,
        documentsFromRepo,
        author.getDocuments(),
        author,
        Author::addDocument,
        Author::removeDocument);
  }

  private void validateRequestedDocumentsExistsOrThrow(
      List<Document> documentsFromRepo, Set<Document> documentsFromClient) {
    log.debug("Validating documents existence");
    ValidationUtils.validateEntitiesExistOrThrow(
        documentsFromRepo, documentsFromClient, "Document(s)");
  }

  private Author getAuthorById(Long id) {
    log.debug("Fetching author by ID: {}", id);
    return authorRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.error("Author not found with ID: {}", id);
              return new ResourceNotFoundException("Author", id);
            });
  }
}
