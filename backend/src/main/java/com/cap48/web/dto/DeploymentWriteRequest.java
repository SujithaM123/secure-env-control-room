package com.cap48.web.dto;

import java.util.Map;

/** The result of verify.py against one environment's freshly-deployed app. */
public record DeploymentWriteRequest(String environment, String imageVersion, boolean verified,
                                      Map<String, Object> verificationDetails) {
}
