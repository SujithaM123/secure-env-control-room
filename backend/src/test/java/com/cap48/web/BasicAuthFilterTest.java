package com.cap48.web;

import com.cap48.secrets.SecretProvider;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class BasicAuthFilterTest {

    private final SecretProvider passwordProvider = new SecretProvider() {
        @Override
        public String get() {
            return "correct-password";
        }

        @Override
        public void rotate(String newValue) {
            throw new UnsupportedOperationException();
        }
    };

    private final BasicAuthFilter filter = new BasicAuthFilter("admin", passwordProvider);

    @Test
    void rejectsWithNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rotate/write-api-key/dev");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Basic realm=\"CAP-48 Rotation\"");
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        MockHttpServletRequest request = basicAuthRequest("admin", "wrong-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }

    @Test
    void rejectsWrongUsername() throws Exception {
        MockHttpServletRequest request = basicAuthRequest("someone-else", "correct-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(chain);
    }

    @Test
    void acceptsCorrectCredentials() throws Exception {
        MockHttpServletRequest request = basicAuthRequest("admin", "correct-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        org.mockito.Mockito.verify(chain).doFilter(request, response);
    }

    private MockHttpServletRequest basicAuthRequest(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/rotate/write-api-key/dev");
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        request.addHeader("Authorization", "Basic " + credentials);
        return request;
    }
}
