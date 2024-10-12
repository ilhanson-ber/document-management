package com.ilhanson.document_management.exceptions;

import java.text.MessageFormat;

public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException(String field, String value) {
        super(MessageFormat.format("{0} with {1} already exists", field, value));
    }
}