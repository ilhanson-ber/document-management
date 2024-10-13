package com.ilhanson.document_management.producers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentEventProducerTest {

    @Value("${application.kafka.topic.document-updated}")
    private String documentUpdatedTopicName;

    @Value("${application.kafka.topic.document-deleted}")
    private String documentDeletedTopicName;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private DocumentEventProducer documentEventProducer;

    @Test
    void shouldSendDocumentUpdatedEvent() {
        // Arrange
        Long documentId = 1L;

        // Act
        documentEventProducer.sendDocumentUpdatedEvent(documentId);

        // Assert
        verify(kafkaTemplate, times(1)).send(documentUpdatedTopicName, documentId.toString());
    }

    @Test
    void shouldSendDocumentDeletedEvent() {
        // Arrange
        Long documentId = 1L;

        // Act
        documentEventProducer.sendDocumentDeletedEvent(documentId);

        // Assert
        verify(kafkaTemplate, times(1)).send(documentDeletedTopicName, documentId.toString());
    }
}
