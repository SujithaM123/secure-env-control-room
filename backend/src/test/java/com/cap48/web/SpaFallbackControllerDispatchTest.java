package com.cap48.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Goes through Spring's real method-based dispatch (standaloneSetup, no
 * DB/context needed) instead of calling the controller method directly -
 * SpaFallbackControllerTest's direct calls would never have caught the
 * original bug, since the bug was in the @GetMapping annotation itself,
 * not the method body.
 */
class SpaFallbackControllerDispatchTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SpaFallbackController()).build();

    @Test
    void postToErrorDispatchesInsteadOf405ing() throws Exception {
        // Simulates the servlet container's error dispatch preserving the
        // original POST method (what happens when WriteApiKeyFilter calls
        // sendError(401) on a POST to a write endpoint with no/bad key).
        mockMvc.perform(post("/error").requestAttr(
                        jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI, "/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
}
