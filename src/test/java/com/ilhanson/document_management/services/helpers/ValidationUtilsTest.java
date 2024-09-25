package com.ilhanson.document_management.services.helpers;

import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilsTest {

    @Test
    void shouldPassValidationWhenAllEntitiesExist() {
        List<MockEntity> entitiesFromRepo = List.of(new MockEntity(1L), new MockEntity(2L), new MockEntity(3L));
        Set<MockEntity> entitiesFromClient = Set.of(new MockEntity(1L), new MockEntity(2L));

        // This should not throw any exception
        ValidationUtils.validateEntitiesExistOrThrow(entitiesFromRepo, entitiesFromClient, "MockEntity");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenSomeEntitiesAreMissing() {
        List<MockEntity> entitiesFromRepo = List.of(new MockEntity(1L), new MockEntity(2L));
        Set<MockEntity> entitiesFromClient = Set.of(new MockEntity(1L), new MockEntity(3L));

        // This should throw ResourceNotFoundException because entity with ID 3 is missing in the repository
        assertThatThrownBy(() ->
                ValidationUtils.validateEntitiesExistOrThrow(entitiesFromRepo, entitiesFromClient, "MockEntity")
        ).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MockEntity")
                .hasMessageContaining("3");
    }

    @Test
    void shouldPassValidationWhenClientSetIsEmpty() {
        List<MockEntity> entitiesFromRepo = List.of(new MockEntity(1L), new MockEntity(2L));
        Set<MockEntity> entitiesFromClient = Set.of(); // Empty client set

        // This should not throw any exception since the client set is empty
        ValidationUtils.validateEntitiesExistOrThrow(entitiesFromRepo, entitiesFromClient, "MockEntity");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAllEntitiesAreMissing() {
        List<MockEntity> entitiesFromRepo = List.of(new MockEntity(1L));
        Set<MockEntity> entitiesFromClient = Set.of(new MockEntity(2L), new MockEntity(3L));

        // This should throw ResourceNotFoundException because entities with IDs 2 and 3 are missing in the repository
        assertThatThrownBy(() ->
                ValidationUtils.validateEntitiesExistOrThrow(entitiesFromRepo, entitiesFromClient, "MockEntity")
        ).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MockEntity")
                .hasMessageContaining("2")
                .hasMessageContaining("3");
    }
}
