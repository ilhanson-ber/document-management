package com.ilhanson.document_management.services.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CollectionUtilsTest {

  @Test
  void shouldExtractIdsFromEntities() {
    Set<MockEntity> entities = Set.of(new MockEntity(1L), new MockEntity(2L), new MockEntity(3L));

    List<Long> ids = CollectionUtils.extractIds(entities);

    assertThat(ids).containsExactlyInAnyOrder(1L, 2L, 3L);
  }

  @Test
  void shouldReturnEmptyListWhenEntitiesAreEmpty() {
    Set<MockEntity> entities = Set.of(); // Empty set

    List<Long> ids = CollectionUtils.extractIds(entities);

    assertThat(ids).isEmpty();
  }

  @Test
  void shouldHandleNullIdsInEntities() {
    Set<MockEntity> entities = Set.of(new MockEntity(null), new MockEntity(2L), new MockEntity(3L));

    List<Long> ids = CollectionUtils.extractIds(entities);

    assertThat(ids).containsExactlyInAnyOrder(null, 2L, 3L);
  }
}
