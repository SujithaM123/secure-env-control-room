package com.cap48.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
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
 *
 * Not @GetMapping: the servlet container's error dispatch preserves the
 * original request's method, so a WriteApiKeyFilter 401 on a POST (or any
 * non-GET write endpoint) lands here as a POST too. A GET-only mapping
 * here meant that dispatch itself failed with 405 "Allow: GET" - masking
 * the real 401 behind a wrong status - confirmed live against a write
 * endpoint called with no credential. No HTTP method restriction here
 * means every method dispatches correctly, including from the
 * write-auth filter.
 */
@RestController
public class SpaFallbackController implements ErrorController {

    @RequestMapping("/error")
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
