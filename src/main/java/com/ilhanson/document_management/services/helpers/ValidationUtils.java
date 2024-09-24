package com.ilhanson.document_management.services.helpers;

import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.models.Identifiable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtils {

    public static <T extends Identifiable> void validateEntitiesExistOrThrow(
            List<T> entitiesFromRepo,
            Set<T> entitiesFromClient,
            String entityName
    ) {
        Set<Long> idsFromRepo = entitiesFromRepo.stream()
                .map(T::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = entitiesFromClient.stream()
                .map(T::getId)
                .collect(Collectors.toSet());

        missingIds.removeAll(idsFromRepo);

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(entityName, missingIds);
        }
    }
}
