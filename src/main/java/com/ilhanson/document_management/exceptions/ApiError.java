package com.ilhanson.document_management.exceptions;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public record ApiError(String message, String status, int statusCode, Instant timestamp,
                       Map<String, String> errorDetails) {
    public ApiError(String message, String status, int statusCode, Instant timestamp) {
        this(message, status, statusCode, timestamp, new HashMap<>());
    }
}
