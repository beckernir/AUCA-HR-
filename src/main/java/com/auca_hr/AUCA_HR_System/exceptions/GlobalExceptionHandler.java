package com.auca_hr.AUCA_HR_System.exceptions;

import com.auca_hr.AUCA_HR_System.utils.StandardResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StandardResponse<Object>> handleRuntimeException(RuntimeException ex) {
        logger.error("RuntimeException: {}", ex.getMessage());
        StandardResponse<Object> response = StandardResponse.builder()
                .message("An error occurred: " + ex.getMessage())
                .data(null)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("ResourceNotFoundException: {}", ex.getMessage());
        StandardResponse<Object> response = StandardResponse.builder()
                .message(ex.getMessage())
                .data(null)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<StandardResponse<Object>> handleInvalidFormatException(InvalidFormatException ex) {
        logger.error("InvalidFormatException: {}", ex.getMessage());
        StandardResponse<Object> response = StandardResponse.builder()
                .message(ex.getMessage())
                .data(null)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("ValidationException: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        StandardResponse<Object> response = StandardResponse.builder()
                .message("Validation failed")
                .data(errors)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.error("ConstraintViolationException: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        StandardResponse<Object> response = StandardResponse.builder()
                .message("Validation failed")
                .data(errors)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("DataIntegrityViolationException: {}", ex.getMessage());
        StandardResponse<Object> response = StandardResponse.builder()
                .message("Database error: " + ex.getRootCause().getMessage())
                .data(null)
                .statusCode(HttpStatus.CONFLICT.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<StandardResponse<Object>> handleFileValidationException(FileValidationException ex) {
        logger.error("FileValidationException: {}", ex.getMessage());
        StandardResponse<Object> response = StandardResponse.builder()
                .message(ex.getMessage())
                .data(null)
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleGeneralException(Exception ex) {
        logger.error("GeneralException: {}", ex.getMessage());
        StandardResponse<Object> response = StandardResponse.builder()
                .message("An unexpected error occurred: " + ex.getMessage())
                .data(null)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
