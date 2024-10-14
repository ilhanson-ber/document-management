package com.ilhanson.document_management.exceptions;

import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException e) {
    HttpStatus notFound = HttpStatus.NOT_FOUND;
    log.warn("Resource not found: {}", e.getMessage());
    ApiError apiError =
        new ApiError(e.getMessage(), notFound.getReasonPhrase(), notFound.value(), Instant.now());
    return new ResponseEntity<>(apiError, notFound);
  }

  @ExceptionHandler(UnprocessableContentException.class)
  public ResponseEntity<ApiError> handleUnprocessableContent(UnprocessableContentException e) {
    HttpStatus unprocessableEntity = HttpStatus.UNPROCESSABLE_ENTITY;
    log.warn("Unprocessable content: {}", e.getMessage());
    ApiError apiError =
        new ApiError(
            e.getMessage(),
            unprocessableEntity.getReasonPhrase(),
            unprocessableEntity.value(),
            Instant.now());
    return new ResponseEntity<>(apiError, unprocessableEntity);
  }

  @ExceptionHandler(AssociationConflictException.class)
  public ResponseEntity<ApiError> handleAssociationConflict(AssociationConflictException e) {
    HttpStatus conflict = HttpStatus.CONFLICT;
    log.warn("Association conflict: {}", e.getMessage());
    ApiError apiError =
        new ApiError(e.getMessage(), conflict.getReasonPhrase(), conflict.value(), Instant.now());
    return new ResponseEntity<>(apiError, conflict);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<ApiError> handleResourceNotFound(DuplicateKeyException e) {
    HttpStatus conflict = HttpStatus.CONFLICT;
    log.warn("Duplicate key conflict: {}", e.getMessage());
    ApiError apiError =
        new ApiError(e.getMessage(), conflict.getReasonPhrase(), conflict.value(), Instant.now());
    return new ResponseEntity<>(apiError, conflict);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    String errorMessage =
        "Validation failed for the request body. See errorDetails for more information.";
    log.debug(
        "Method argument validation failed: {}. Field errors: {}", e.getMessage(), fieldErrors);

    ApiError apiError =
        new ApiError(
            errorMessage,
            badRequest.getReasonPhrase(),
            badRequest.value(),
            Instant.now(),
            fieldErrors);
    return new ResponseEntity<>(apiError, badRequest);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException e) {
    HttpStatus badRequest = HttpStatus.BAD_REQUEST;

    String errorMessage =
        "Validation failed for the request path. See errorDetails for more information.";
    log.debug(
        "Method argument type mismatch: parameter name = {}, error = {}",
        e.getName(),
        e.getMessage());

    Map<String, String> errorDetails = new HashMap<>();
    errorDetails.put(e.getName(), e.getMessage());

    ApiError apiError =
        new ApiError(
            errorMessage,
            badRequest.getReasonPhrase(),
            badRequest.value(),
            Instant.now(),
            errorDetails);
    return new ResponseEntity<>(apiError, badRequest);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException e) {
    HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
    log.warn("Bad credentials error: {}", e.getMessage());
    ApiError apiError =
        new ApiError(
            e.getMessage(), unauthorized.getReasonPhrase(), unauthorized.value(), Instant.now());
    return new ResponseEntity<>(apiError, unauthorized);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ApiError> handleJwt(JwtException e) {
    HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
    log.warn("Bad JWT Token: {}", e.getMessage());
    ApiError apiError =
        new ApiError(
            "Authorization token error",
            unauthorized.getReasonPhrase(),
            unauthorized.value(),
            Instant.now());
    return new ResponseEntity<>(apiError, unauthorized);
  }
}
