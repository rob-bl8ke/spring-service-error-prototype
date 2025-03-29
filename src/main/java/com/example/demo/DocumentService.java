package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class DocumentService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public DocumentService(RestTemplateBuilder restTemplateBuilder,
                           ObjectMapper objectMapper,
                           @Value("${downstream.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    public DocumentResponse fetchDocument(String documentId) {
        String url = baseUrl + "/documents/" + documentId;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), DocumentResponse.class);
            }

            throw new DownstreamServerException("Unexpected status", null);

        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e);
        } catch (IOException e) {
            throw new DownstreamClientException("Error parsing response", e);
        }
    }

    private DocumentResponse handleHttpStatusCodeException(HttpStatusCodeException e) {
        try {
            ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
            if (e.getStatusCode().is4xxClientError()) {
                throw new DownstreamClientException(error.getMessage(), e);
            } else {
                throw new DownstreamServerException(error.getMessage(), e);
            }
        } catch (IOException ex) {
            throw new DownstreamServerException("Failed to parse error response", ex);
        }
    }
}
