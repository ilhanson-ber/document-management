package com.ilhanson.document_management.exceptions;

public class UnprocessableContentException extends RuntimeException {
  public UnprocessableContentException() {
    super("ID in request path and ID in request body should match");
  }
}
