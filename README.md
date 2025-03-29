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

---

## 🧪 Running Tests

### Unit Tests
```bash
mvn test -Dtest=DocumentServiceTest
```

### Full Integration Tests with MockServer
```bash
mvn test -Dtest=DocumentServiceIntegrationTest
```

---

## ✅ Features Demonstrated
- DTO parsing for both success and error cases
- Custom exceptions (`DownstreamClientException`, `DownstreamServerException`)
- Global exception handling with `@RestControllerAdvice`
- Configurable HTTP base URL for downstream
- Full end-to-end tests with MockServer (via Testcontainers)


