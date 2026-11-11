package com.cap48.web.dto;

import java.util.List;

/**
 * One drift.py comparison between two environments, carrying every
 * differing/missing key found for that pair in one call. Values must
 * already be redacted client-side (drift.py's own safe_display) before
 * they're sent — this endpoint stores whatever string it's given verbatim.
 */
public record DriftFindingsWriteRequest(String envA, String envB, String commitSha, List<Item> findings) {

    public record Item(String settingKey, String valueA, String valueB, boolean allowed) {
    }
}
