package com.ilhanson.document_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException e) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ApiError apiError = new ApiError(e.getMessage(), notFound.getReasonPhrase(), notFound.value(), Instant.now());
        return new ResponseEntity<>(apiError, notFound);
    }

    @ExceptionHandler(UnprocessableContentException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(UnprocessableContentException e) {
        HttpStatus unprocessableEntity = HttpStatus.UNPROCESSABLE_ENTITY;
        ApiError apiError = new ApiError(e.getMessage(), unprocessableEntity.getReasonPhrase(), unprocessableEntity.value(), Instant.now());
        return new ResponseEntity<>(apiError, unprocessableEntity);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(MethodArgumentNotValidException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiError apiError = new ApiError(e.getMessage(), badRequest.getReasonPhrase(), badRequest.value(), Instant.now(), fieldErrors);
        return new ResponseEntity<>(apiError, badRequest);
    }

}