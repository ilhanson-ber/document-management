package com.ilhanson.document_management.exceptions;

public class UnprocessableContentException extends RuntimeException {
    public UnprocessableContentException(String message) {
        super(message);
    }
}