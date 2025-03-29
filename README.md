# Document Service Downstream Error Handling Prototype

A Spring Boot microservice prototype that fetches documents from a downstream service using RestTemplate. Includes:

- JSON response mapping to DTOs
- Custom exception handling
- Unit tests with Mockito
- Integration tests using Testcontainers + MockServer

## ⚙️ Configuration

Set the downstream base URL in `application.yml`:

```yaml
downstream:
  base-url: http://localhost:3001
```

Testcontainers will override this automatically during integration tests.

### VS Code

`launch.json`

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Service (profile=local)",
      "request": "launch",
      "cwd": "${workspaceFolder}",
      "mainClass": "com.example.demo.DemoApplication",
      "projectName": "document-service-prototype",
      "env": {
        "SPRING_PROFILES_ACTIVE": "local"
      }
    }
  ]
}
```

`settings.json`

```json
{
    "java.configuration.updateBuildConfiguration": "automatic"
}
```

## 🚀 Running the App

```bash
mvn spring-boot:run -D"spring-boot.run.profiles=local"
```

Access the test endpoint:

```
GET http://localhost:8080/documents/{id}
```

## 🧪 Running Tests

### Unit Tests
```bash
mvn test -Dtest=DocumentServiceTest
```

### Full Integration Tests with MockServer
```bash
mvn test -Dtest=DocumentServiceIntegrationTest
```
>IMPORTANT: You must have Docker or some compatible container engine running.

## ✅ Features Demonstrated
- DTO parsing for both success and error cases
- Custom exceptions (`DownstreamClientException`, `DownstreamServerException`)
- Global exception handling with `@RestControllerAdvice`
- Configurable HTTP base URL for downstream
- Full end-to-end tests with MockServer (via Testcontainers)

# The Tests

- Unit Tests (DocumentServiceTest.java): Test the DocumentService in isolation by mocking dependencies. They are fast and focus on the internal logic of the service.

- Integration Tests (DocumentServiceIntegrationTest.java): Test the DocumentService in integration with the downstream service (mocked using MockServer). They validate end-to-end behavior and Spring configuration.

## How the Unit Tests Work

The DocumentServiceTest class contains unit tests for the DocumentService class. These tests verify the behavior of DocumentService in isolation by mocking its dependencies (e.g., RestTemplate and RestTemplateBuilder).

- `shouldReturnValidDocumentResponse` - This test verifies that the DocumentService correctly handles a successful response (HTTP 200) from the downstream service and parses the response body into a DocumentResponse object.

- `shouldThrowClientErrorException` - This test verifies that the DocumentService correctly handles a client error response (HTTP 400) from the downstream service by throwing a DownstreamClientException.

- `shouldThrowServerErrorException` - This test verifies that the DocumentService correctly handles a server error response (HTTP 500) from the downstream service by throwing a DownstreamServerException.

 By mocking dependencies, these tests focus solely on the logic within DocumentService.

## How the Integration Tests Work

### `DocumentServiceIntegrationTest`

The tests in the DocumentServiceIntegrationTest class are integration tests for the DocumentService class. They verify how the DocumentService interacts with a downstream service (mocked using MockServer) and handles different types of responses. Here's a detailed explanation of what each test is doing and how it works:

#### Test: shouldReturnValidDocumentResponse

This test verifies that the DocumentService correctly handles a successful response (HTTP 200) from the downstream service and parses the response body into a DocumentResponse object.

The MockServerClient is configured to respond to a GET request to /documents/1 with:
- HTTP status code 200.
- Content-Type application/json.
- A JSON body: { "name": "doc1", "type": "pdf", "format": "A4" }.


The fetchDocument("1") method of DocumentService is called and the test asserts that the DocumentResponse object returned by the service has the expected name value ("doc1").

The other tests work in a similar fashion, testing different responses.

The tests cover the:
- Happy Path: The shouldReturnValidDocumentResponse test ensures that the service works correctly when the downstream service returns a successful response.
- Error Handling: The shouldThrowDownstreamClientException and shouldThrowDownstreamServerException tests ensure that the service handles client and server errors appropriately by throwing the correct exceptions.

### `MockServerContainerInitializer`

The `MockServerContainerInitializer` class is a custom implementation of Spring's `ApplicationContextInitializer` interface. It is used to programmatically modify the Spring application context before it is refreshed. Specifically, this class starts a `MockServerContainer` (a Testcontainers container for MockServer) and overrides the `downstream.base-url` property in the Spring environment to point to the running MockServer instance.
- `MockServerContainer`: This is a Testcontainers container that runs a MockServer instance. It is initialized with the Docker image mockserver/mockserver:5.15.0.
- Static Initialization Block: The container is started as soon as the class is loaded. This ensures that the container is running before the Spring application context is initialized.
- `ApplicationContextInitializer`: This interface allows you to customize the Spring application context before it is refreshed. It is particularly useful for setting up test-specific configurations, such as overriding properties or initializing beans.

- `mockServerContainer.getEndpoint()`: This method returns the base URL of the running MockServer container (e.g., http://localhost:12345), where 12345 is the dynamically assigned port.
- `TestPropertyValues.of(...)`: This utility method creates a set of key-value pairs to override properties in the Spring environment.
- `.applyTo(...)`: This applies the overridden properties to the Spring environment of the ConfigurableApplicationContext.

In this case, the downstream.base-url property is overridden to point to the MockServer instance. This ensures that any component in your application that uses the downstream.base-url property (e.g., DocumentService) will connect to the MockServer instead of the actual downstream service.

The `MockServerContainerInitializer` is registered in the test class using the `@ContextConfiguration` annotation:

```java
@ContextConfiguration(initializers = {MockServerContainerInitializer.class})
```
This tells Spring to use the MockServerContainerInitializer to customize the application context for the test. When the test starts:

- The MockServerContainer is started.
- The downstream.base-url property is overridden to point to the MockServer instance.

#### How It Overrides Your Configuration
Normally, the downstream.base-url property would be loaded from your application.yml or application-{profile}.yml file.

However, the MockServerContainerInitializer explicitly overrides this property in the Spring environment before the application context is refreshed. This ensures that the MockServer URL is used instead of the value from your configuration files.

#### Advantages of This Approach
- Dynamic Configuration: The downstream.base-url is dynamically set to the MockServer's URL, which is useful for integration tests.
- Isolation: Your tests are isolated from the actual downstream service, as they use the MockServer instead.
- Reusability: The initializer can be reused across multiple test classes by simply adding the @ContextConfiguration annotation.

#### Potential Issues
- Static Initialization: The MockServerContainer is started in a static block, which means it will run for the entire lifecycle of the test suite. If you need to stop and restart the container for each test, you would need a different approach.
- Port Conflicts: If the container fails to start due to a port conflict, the test will fail. Ensure that the MockServer container is properly configured to use a dynamic port.

# References

- [Good reference for `testcontainers` samples](https://github.com/sivaprasadreddy/testcontainers-samples)
  - [See for Mock Server](https://github.com/sivaprasadreddy/testcontainers-samples/tree/main/spring-boot-mockserver-demo/src/test/java/com/sivalabs/tcdemo)