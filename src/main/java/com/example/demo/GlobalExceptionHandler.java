package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DownstreamClientException.class)
    public ResponseEntity<String> handleClientError(DownstreamClientException e) {
        logger.warn("Client error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Client error occurred");
    }

    @ExceptionHandler(DownstreamServerException.class)
    public ResponseEntity<String> handleServerError(DownstreamServerException e) {
        logger.error("Server error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Server error occurred");
    }
}
