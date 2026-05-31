package com.taxease.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        errorResponse.put("error", e.getMessage());
        
        // Determine appropriate HTTP status based on error message
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Invalid email or password")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (e.getMessage().contains("User already exists")) {
                status = HttpStatus.CONFLICT;
            } else if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("Server error") || e.getMessage().contains("Internal")) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "An error occurred. Please try again later.");
        errorResponse.put("error", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

