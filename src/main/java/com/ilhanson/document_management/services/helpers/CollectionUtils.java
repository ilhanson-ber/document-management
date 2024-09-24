package com.ilhanson.document_management.services.helpers;

import com.ilhanson.document_management.models.Identifiable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtils {
    public static <T extends Identifiable> List<Long> extractIds(Collection<T> entities) {
        return entities.stream()
                .map(Identifiable::getId)
                .collect(Collectors.toList());
    }
}
