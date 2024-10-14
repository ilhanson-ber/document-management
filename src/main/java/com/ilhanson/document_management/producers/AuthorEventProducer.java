package com.ilhanson.document_management.producers;

import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.mappers.AuthorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorEventProducer {
  @Value("${application.kafka.topic.author-updated}")
  private String authorUpdatedTopicName;

  @Value("${application.kafka.topic.author-deleted}")
  private String authorDeletedTopicName;

  private final KafkaTemplate<String, String> kafkaTemplate;

  private final AuthorMapper authorMapper;

  public void sendAuthorUpdatedEvent(AuthorDetailsDTO authorDetailsDTO) {
    log.info("Preparing to send Author Updated Event for Author ID: {}", authorDetailsDTO.getId());
    try {
      kafkaTemplate.send(authorUpdatedTopicName, authorMapper.toJson(authorDetailsDTO));
      log.info(
          "Successfully sent Author Updated Event for Author ID: {}", authorDetailsDTO.getId());
    } catch (Exception e) {
      log.error(
          "Failed to send Author Updated Event for Author ID: {}", authorDetailsDTO.getId(), e);
    }
  }

  public void sendAuthorDeletedEvent(Long authorId) {
    log.info("Preparing to send Author Deleted Event for Author ID: {}", authorId);
    try {
      kafkaTemplate.send(authorDeletedTopicName, authorId.toString());
      log.info("Successfully sent Author Deleted Event for Author ID: {}", authorId);
    } catch (Exception e) {
      log.error("Failed to send Author Deleted Event for Author ID: {}", authorId, e);
    }
  }
}
