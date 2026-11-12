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
import java.nio.charset.StandardCharsets;

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
 *
 * For /api and /actuator paths, the original status code (read from
 * {@link RequestDispatcher#ERROR_STATUS_CODE}) is preserved rather than
 * hardcoded to 404 - that dispatch doesn't only happen for genuinely
 * unmatched routes, it also happens for WriteApiKeyFilter's 401, and
 * hardcoding 404 here silently turned every such 401 into a 404 instead -
 * confirmed live immediately after fixing the 405 above.
 *
 * For /api and /actuator paths, the real error message (read from
 * {@link RequestDispatcher#ERROR_MESSAGE} - whatever a filter's
 * sendError(status, message) or a controller's ResponseStatusException
 * reason actually said) is returned as the plain-text body when present,
 * instead of an empty one - so a caller (the rotation login form, for
 * one) can show the real reason a request failed, not a generic one.
 */
@RestController
public class SpaFallbackController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<byte[]> handleError(HttpServletRequest request) throws IOException {
        Object pathAttribute = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = pathAttribute != null ? pathAttribute.toString() : "";

        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttribute instanceof Integer ? (Integer) statusAttribute : HttpStatus.NOT_FOUND.value();

        if (path.startsWith("/api") || path.startsWith("/actuator")) {
            Object messageAttribute = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            String message = messageAttribute != null ? messageAttribute.toString() : null;
            if (message != null && !message.isBlank()) {
                return ResponseEntity.status(status).contentType(MediaType.TEXT_PLAIN)
                        .body(message.getBytes(StandardCharsets.UTF_8));
            }
            return ResponseEntity.status(status).build();
        }

        ClassPathResource indexHtml = new ClassPathResource("static/index.html");
        if (!indexHtml.exists()) {
            return ResponseEntity.status(status).build();
        }
        byte[] body = indexHtml.getInputStream().readAllBytes();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML).body(body);
    }
}
