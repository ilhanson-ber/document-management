package com.ilhanson.document_management.producers;

import static org.mockito.Mockito.*;

import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.mappers.AuthorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class AuthorEventProducerTest {
  @Value("${application.kafka.topic.author-updated}")
  private String authorUpdatedTopicName;

  @Value("${application.kafka.topic.author-deleted}")
  private String authorDeletedTopicName;

  @Mock private KafkaTemplate<String, String> kafkaTemplate;

  @Mock private AuthorMapper authorMapper;

  @InjectMocks private AuthorEventProducer authorEventProducer;

  @Test
  void shouldSendAuthorUpdatedEvent() throws Exception {
    // Arrange
    AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", null);
    String authorJson = "{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\"}";
    when(authorMapper.toJson(authorDetailsDTO)).thenReturn(authorJson);

    // Act
    authorEventProducer.sendAuthorUpdatedEvent(authorDetailsDTO);

    // Assert
    verify(kafkaTemplate, times(1))
        .send(authorUpdatedTopicName, authorJson); // Ensure topic name matches configuration
    verify(authorMapper, times(1)).toJson(authorDetailsDTO);
  }

  @Test
  void shouldSendAuthorDeletedEvent() {
    // Arrange
    Long authorId = 1L;

    // Act
    authorEventProducer.sendAuthorDeletedEvent(authorId);

    // Assert
    verify(kafkaTemplate, times(1)).send(authorDeletedTopicName, authorId.toString());
  }
}
