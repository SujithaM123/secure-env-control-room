package com.cap48.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * React Router owns client-side routes like /compare and /history/<sha> -
 * there's no server-side mapping or static file for them, so without this
 * a direct reload or deep link 404s with Spring's Whitelabel page instead
 * of handing off to the SPA. Overriding Spring Boot's default
 * BasicErrorController (same /error path) lets this serve index.html for
 * any unmatched path outside /api and /actuator, so React Router can take
 * over and render the right page; a genuinely unknown /api/... path still
 * gets a normal 404 rather than being masked as HTML.
 */
@RestController
public class SpaFallbackController implements ErrorController {

    @GetMapping("/error")
    public ResponseEntity<byte[]> handleError(HttpServletRequest request) throws IOException {
        Object attribute = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = attribute != null ? attribute.toString() : "";

        if (path.startsWith("/api") || path.startsWith("/actuator")) {
            return ResponseEntity.notFound().build();
        }

        ClassPathResource indexHtml = new ClassPathResource("static/index.html");
        if (!indexHtml.exists()) {
            return ResponseEntity.notFound().build();
        }
        byte[] body = indexHtml.getInputStream().readAllBytes();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML).body(body);
    }
}
