package com.example.demo;

public class DownstreamServerException extends RuntimeException {
    public DownstreamServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
