package com.ilhanson.document_management.services.helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class AssociationUtils {
  public static <T> boolean shouldUpdateAssociations(Set<T> requested, Set<T> current) {
    return !requested.equals(current);
  }

  public static <T, U> void associateEntitiesWithOwner(
      Set<T> entitiesFromClient,
      List<T> entitiesFromRepo,
      Set<T> entitiesFromOwner,
      U owner,
      BiConsumer<U, T> addEntityToOwner,
      BiConsumer<U, T> removeEntityFromOwner) {
    // we need to clone the current collection from the owner
    // because we will modify the state while
    // we are adding and removing associations.
    // otherwise: ConcurrentModificationException
    Set<T> entitiesBeforeRequest = new HashSet<>(entitiesFromOwner);

    // Determine entities to add and remove
    Set<T> entitiesToAdd = new HashSet<>(entitiesFromClient);
    entitiesToAdd.removeAll(entitiesBeforeRequest);

    Set<T> entitiesToRemove = new HashSet<>(entitiesBeforeRequest);
    entitiesToRemove.removeAll(entitiesFromClient);

    // Add new entities to the owner
    for (T entity : entitiesFromRepo) {
      if (entitiesToAdd.contains(entity)) {
        addEntityToOwner.accept(owner, entity);
      }
    }

    // Remove entities from the owner
    for (T entity : entitiesBeforeRequest) {
      if (entitiesToRemove.contains(entity)) {
        removeEntityFromOwner.accept(owner, entity);
      }
    }
  }
}
