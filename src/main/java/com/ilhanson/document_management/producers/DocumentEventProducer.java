package com.ilhanson.document_management.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentEventProducer {
    @Value("${application.kafka.topic.document-updated}")
    private String documentUpdatedTopicName;

    @Value("${application.kafka.topic.document-deleted}")
    private String documentDeletedTopicName;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendDocumentUpdatedEvent(Long documentId) {
        log.info("Preparing to send Document Updated Event for Document ID: {}", documentId);
        try {
            kafkaTemplate.send(documentUpdatedTopicName, documentId.toString());
            log.info("Successfully sent Document Updated Event for Document ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to send Document Updated Event for Document ID: {}", documentId, e);
        }
    }

    public void sendDocumentDeletedEvent(Long documentId) {
        log.info("Preparing to send Document Deleted Event for Document ID: {}", documentId);
        try {
            kafkaTemplate.send(documentDeletedTopicName, documentId.toString());
            log.info("Successfully sent Document Deleted Event for Document ID: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to send Document Deleted Event for Document ID: {}", documentId, e);
        }
    }
}
