package com.aegis.tests.orchestrator.shared.config;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to serve ReDoc API documentation.
 *
 * ReDoc provides a clean, modern UI for API documentation.
 * Access at: http://localhost:8080/redoc
 */
@RestController
@Slf4j
public class RedocController {

    private final ServletContext servletContext;

    public RedocController(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @GetMapping(value = "/redoc", produces = "text/html")
    public String getRedocDocumentation() {
        String contextPath = servletContext.getContextPath();
        String redocUrl = contextPath + "/v3/api-docs";

        log.debug("Context path for Redoc: {}", contextPath);
        log.debug("Redoc documentation spec URL: {}", redocUrl);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Aegis Test Orchestrator API - Documentation</title>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                        }
                    </style>
                </head>
                <body>
                    <redoc spec-url='""" + redocUrl + """
                '
                           lazy-rendering="true"
                           hide-download-button="false"
                           scroll-y-offset="0"
                           theme='{"colors": {"primary": {"main": "#3b82f6"}}}'>
                    </redoc>
                    <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
                </body>
                </html>
                """;
    }
}
