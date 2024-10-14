package com.ilhanson.document_management.consumers;

import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.dtos.DocumentDTO;
import com.ilhanson.document_management.mappers.AuthorMapper;
import com.ilhanson.document_management.services.AuthorService;
import com.ilhanson.document_management.services.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorEventConsumer {
  private final AuthorService authorService;
  private final DocumentService documentService;

  private final AuthorMapper authorMapper;

  // This implementation deletes the documents that are
  // associated with the author when the event is created.
  // It doesn't take into account any changes made between
  // the event creation and consumption.
  // After documents, it deletes the author.
  // See implementation notes and business cases in the documentation
  // for details.
  @KafkaListener(topics = "${application.kafka.topic.author-updated}")
  public void handleAuthorUpdated(String event) {
    try {
      log.info("Author updated event received: {}", event);
      AuthorDetailsDTO authorDetailsDTO = authorMapper.toDetailsDTO(event);

      for (DocumentDTO document : authorDetailsDTO.getDocuments()) {
        try {
          documentService.deleteDocument(document.getId());
        } catch (Exception e) {
          log.error("Failed to delete document with ID: {}", document.getId(), e);
        }
      }

      try {
        authorService.deleteAuthor(authorDetailsDTO.getId());
      } catch (Exception e) {
        log.error("Failed to delete author with ID: {}", authorDetailsDTO.getId(), e);
      }
    } catch (Exception e) {
      log.error("Failed to process Author Updated Event for: {}", event, e);
    }
  }

  @KafkaListener(topics = "${application.kafka.topic.author-deleted}")
  public void handleAuthorDeleted(String event) {
    log.info("Author deleted event received: {}", event);
  }
}
