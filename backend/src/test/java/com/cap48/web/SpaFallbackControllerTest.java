package com.cap48.web;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * No Spring context needed - this exercises the controller method directly
 * against a MockHttpServletRequest, since booting the full app requires a
 * reachable database this test shouldn't depend on.
 */
class SpaFallbackControllerTest {

    private final SpaFallbackController controller = new SpaFallbackController();

    @Test
    void forwardsToIndexHtmlForAClientSideRoute() throws Exception {
        ResponseEntity<byte[]> response = controller.handleError(requestFor("/compare"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_HTML);
        assertThat(new String(response.getBody())).contains("<div id=\"root\">");
    }

    @Test
    void forwardsToIndexHtmlForANestedClientSideRoute() throws Exception {
        ResponseEntity<byte[]> response = controller.handleError(requestFor("/history/abc1234567"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new String(response.getBody())).contains("<div id=\"root\">");
    }

    @Test
    void leavesAnUnknownApiPathAsA404() throws Exception {
        ResponseEntity<byte[]> response = controller.handleError(requestFor("/api/does-not-exist"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void leavesAnUnknownActuatorPathAsA404() throws Exception {
        ResponseEntity<byte[]> response = controller.handleError(requestFor("/actuator/does-not-exist"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void preservesA401FromWriteApiKeyFilterOnAnApiPathInsteadOfMaskingItAs404() throws Exception {
        MockHttpServletRequest request = requestFor("/api/deployments");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 401);

        ResponseEntity<byte[]> response = controller.handleError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void includesTheRealErrorMessageOnAnApiPathWhenPresent() throws Exception {
        MockHttpServletRequest request = requestFor("/api/rotate/write-api-key/dev");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 401);
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Missing or invalid credentials");

        ResponseEntity<byte[]> response = controller.handleError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(new String(response.getBody())).isEqualTo("Missing or invalid credentials");
    }

    private MockHttpServletRequest requestFor(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, path);
        return request;
    }
}
