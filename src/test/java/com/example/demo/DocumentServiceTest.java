package com.example.demo;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// --- Unit Test for DocumentService ---
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private RestTemplateBuilder restTemplateBuilder; // Mock RestTemplateBuilder
    @InjectMocks private DocumentService documentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        // Configure the mock RestTemplateBuilder to return the mock RestTemplate
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        // Initialize the DocumentService with the mocked dependencies
        documentService = new DocumentService(restTemplateBuilder, objectMapper, "http://mock-base-url");
    }

    @Test
    void shouldReturnValidDocumentResponse() throws Exception {
        String json = """
            { "name": "doc1", "type": "pdf", "format": "A4" }
        """;

        ResponseEntity<String> entity = new ResponseEntity<>(json, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
               .thenReturn(entity);

        DocumentResponse result = documentService.fetchDocument("1");
        assertEquals("doc1", result.getName());
    }

    @Test
    void shouldThrowClientErrorException() {
        String error = """
            { "errorCode": "NOT_FOUND", "message": "Document not found" }
        """;

        HttpClientErrorException ex = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
            error.getBytes(), StandardCharsets.UTF_8);

        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
               .thenThrow(ex);

        DownstreamClientException thrown = assertThrows(DownstreamClientException.class, () -> {
            documentService.fetchDocument("404");
        });

        assertTrue(thrown.getMessage().contains("Document not found"));
    }

    @Test
    void shouldThrowServerErrorException() {
        String error = """
            { "errorCode": "INTERNAL", "message": "Server crash" }
        """;

        HttpServerErrorException ex = HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", HttpHeaders.EMPTY,
            error.getBytes(), StandardCharsets.UTF_8);

        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
               .thenThrow(ex);

        DownstreamServerException thrown = assertThrows(DownstreamServerException.class, () -> {
            documentService.fetchDocument("500");
        });

        assertTrue(thrown.getMessage().contains("Server crash"));
    }
}