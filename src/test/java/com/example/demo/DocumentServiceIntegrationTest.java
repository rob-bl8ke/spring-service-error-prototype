package com.example.demo;

import static com.example.demo.MockServerContainerInitializer.mockServerContainer;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import org.mockserver.model.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @ActiveProfiles("local") // Explicitly set the active profile
@ContextConfiguration(initializers = {MockServerContainerInitializer.class})
public class DocumentServiceIntegrationTest {

    private final MockServerClient mockClient =
            new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort());

    @Autowired private DocumentService documentService;

    @Test
    void shouldReturnValidDocumentResponse() {
        mockClient.when(request().withMethod("GET").withPath("/documents/1"))
                  .respond(response().withStatusCode(200)
                                   .withContentType(MediaType.APPLICATION_JSON)
                                   .withBody("""
                                        { "name": "doc1", "type": "pdf", "format": "A4" }
                                   """));

        DocumentResponse result = documentService.fetchDocument("1");
        assertEquals("doc1", result.getName());
    }

    @Test
    void shouldThrowDownstreamClientException() {
        mockClient.when(request().withMethod("GET").withPath("/documents/404"))
                  .respond(response().withStatusCode(400)
                                   .withContentType(MediaType.APPLICATION_JSON)
                                   .withBody("""
                                        { "errorCode": "NOT_FOUND", "message": "No such doc" }
                                   """));

        DownstreamClientException ex = assertThrows(DownstreamClientException.class, () -> {
            documentService.fetchDocument("404");
        });

        assertTrue(ex.getMessage().contains("No such doc"));
    }

    @Test
    void shouldThrowDownstreamServerException() {
        mockClient.when(request().withMethod("GET").withPath("/documents/500"))
                  .respond(response().withStatusCode(500)
                                   .withContentType(MediaType.APPLICATION_JSON)
                                   .withBody("""
                                        { "errorCode": "FAIL", "message": "Boom" }
                                   """));

        DownstreamServerException ex = assertThrows(DownstreamServerException.class, () -> {
            documentService.fetchDocument("500");
        });

        assertTrue(ex.getMessage().contains("Boom"));
    }
}
