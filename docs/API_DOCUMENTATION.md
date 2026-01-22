# API Documentation

## Available Documentation UIs

### 🎯 Redoc (Recommended)
**Clean, modern, and easy to read**

📍 **URL**: http://localhost:8080/redoc

**Features:**
- Clean three-column layout
- Excellent navigation with sidebar
- Better readability for complex schemas
- Search functionality
- Dark mode support
- Export to PDF

---

### 📘 Swagger UI (Alternative)
**Interactive API testing**

📍 **URL**: http://localhost:8080/swagger-ui.html

**Features:**
- Try out endpoints directly
- Test requests with sample data
- View responses in real-time
- Good for development and debugging

---

### 📄 OpenAPI Spec (Raw JSON)
**For integration with tools**

📍 **URL**: http://localhost:8080/v3/api-docs

Use this for:
- Postman/Insomnia import
- Code generation tools
- CI/CD pipeline integration
- Custom documentation tools

---

## Quick Start

1. **Start the application:**
   ```bash
   mvnw spring-boot:run
   ```

2. **Open Redoc in your browser:**
   ```
   http://localhost:8080/redoc
   ```

3. **Explore the API:**
   - Browse endpoints by tags (left sidebar)
   - Click on operations to see details
   - Review request/response schemas
   - Try example payloads

---

## Customization

### Change Redoc Theme
Edit `application.properties`:
```properties
# Enable dark mode by default
springdoc.redoc.theme=dark
```

### Disable Swagger UI (keep only Redoc)
```properties
springdoc.swagger-ui.enabled=false
```

### Change Documentation Paths
```properties
springdoc.redoc.path=/docs
springdoc.api-docs.path=/openapi.json
```

---

## Tags Organization

The API is organized by the following tags:

| Tag | Description |
|-----|-------------|
| **Test Projects** | Root container for test suites |
| **Specifications** | AI-driven test specifications |
| **Environments** | Test environment management |
| **API Calls** | Endpoint catalog |
| **Auth Profiles** | Authentication credentials |
| **Base URLs** | Environment-specific base URLs |
| **Domains** | Semantic grouping of endpoints |

---

## Tips for Better Documentation

### For Developers

When creating endpoints, always include:

```java
@Operation(
    summary = "Short description",
    description = """
        Detailed explanation with:
        - Business rules
        - Validation requirements
        - Example use cases
        """
)
@ApiResponse(responseCode = "201", description = "Success message")
@ApiResponse(responseCode = "400", description = "Validation error")
```

### For API Consumers

- **Start with the overview** in Redoc's introduction
- **Check request examples** before making calls
- **Review error responses** to handle failures properly
- **Use the search** (Ctrl+F) to find specific endpoints quickly

---

## Troubleshooting

### Redoc not loading?

1. Check if the app is running: `curl http://localhost:8080/actuator/health`
2. Verify OpenAPI spec is available: `curl http://localhost:8080/v3/api-docs`
3. Clear browser cache and reload

### Missing endpoints?

Make sure controllers have:
- `@RestController` annotation
- `@RequestMapping` with path
- `@Tag` for grouping in docs

---

## Production Considerations

⚠️ **Before deploying to production:**

1. **Add authentication** to documentation endpoints
2. **Disable Swagger UI** if not needed (keep Redoc only)
3. **Review descriptions** for sensitive information
4. **Update server URLs** in OpenApiConfig
5. **Consider rate limiting** for public docs

Example production config:
```properties
# Disable Swagger UI
springdoc.swagger-ui.enabled=false

# Keep Redoc with authentication
springdoc.redoc.enabled=true

# Disable try-it-out features
springdoc.swagger-ui.supportedSubmitMethods=
```
