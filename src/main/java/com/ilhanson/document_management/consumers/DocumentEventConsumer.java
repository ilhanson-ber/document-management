package com.ilhanson.document_management.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentEventConsumer {

  @KafkaListener(topics = "${application.kafka.topic.document-updated}")
  public void handleDocumentUpdated(String event) {
    log.info("Document updated event received: {}", event);
  }

  @KafkaListener(topics = "${application.kafka.topic.document-deleted}")
  public void handleDocumentDeleted(String event) {
    log.info("Document deleted event received: {}", event);
  }
}
