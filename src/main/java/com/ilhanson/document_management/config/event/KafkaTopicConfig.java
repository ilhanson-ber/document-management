package com.ilhanson.document_management.config.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
  @Value("${application.kafka.topic.author-updated}")
  private String authorUpdatedTopicName;

  @Value("${application.kafka.topic.author-deleted}")
  private String authorDeletedTopicName;

  @Value("${application.kafka.topic.document-updated}")
  private String documentUpdatedTopicName;

  @Value("${application.kafka.topic.document-deleted}")
  private String documentDeletedTopicName;

  @Bean
  public NewTopic authorUpdatedTopic() {
    return TopicBuilder.name(authorUpdatedTopicName).partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic authorDeletedTopic() {
    return TopicBuilder.name(authorDeletedTopicName).partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic documentUpdatedTopic() {
    return TopicBuilder.name(documentUpdatedTopicName).partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic documentDeletedTopic() {
    return TopicBuilder.name(documentDeletedTopicName).partitions(1).replicas(1).build();
  }
}
