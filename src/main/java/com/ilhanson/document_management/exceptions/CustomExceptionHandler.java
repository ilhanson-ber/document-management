package com.ilhanson.document_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
    public ResponseEntity<ApiError> handleUnprocessableContent(UnprocessableContentException e) {
        HttpStatus unprocessableEntity = HttpStatus.UNPROCESSABLE_ENTITY;
        ApiError apiError = new ApiError(e.getMessage(), unprocessableEntity.getReasonPhrase(), unprocessableEntity.value(), Instant.now());
        return new ResponseEntity<>(apiError, unprocessableEntity);
    }

    @ExceptionHandler(AssociationConflictException.class)
    public ResponseEntity<ApiError> handleAssociationConflict(AssociationConflictException e) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        ApiError apiError = new ApiError(e.getMessage(), conflict.getReasonPhrase(), conflict.value(), Instant.now());
        return new ResponseEntity<>(apiError, conflict);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String errorMessage = "Validation failed for the request body. See errorDetails for more information.";
        ApiError apiError = new ApiError(errorMessage, badRequest.getReasonPhrase(), badRequest.value(), Instant.now(), fieldErrors);
        return new ResponseEntity<>(apiError, badRequest);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        String errorMessage = "Validation failed for the request path. See errorDetails for more information.";

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put(e.getName(), e.getMessage());

        ApiError apiError = new ApiError(errorMessage, badRequest.getReasonPhrase(), badRequest.value(), Instant.now(), errorDetails);
        return new ResponseEntity<>(apiError, badRequest);
    }

}