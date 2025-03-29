package com.example.demo;

public class DownstreamClientException extends RuntimeException {
    public DownstreamClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
