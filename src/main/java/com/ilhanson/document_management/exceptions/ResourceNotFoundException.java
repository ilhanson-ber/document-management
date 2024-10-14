package com.ilhanson.document_management.exceptions;

import java.text.MessageFormat;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String entityName, Object id) {
    super(MessageFormat.format("{0} with ID {1} does not exist", entityName, id));
  }
}
