package com.cap48.web.dto;

import java.util.List;

/** One validate_config.py run for one environment. */
public record ValidationResultWriteRequest(String environment, String status, List<String> errors, String commitSha) {
}
