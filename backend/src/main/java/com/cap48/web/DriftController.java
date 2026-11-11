package com.cap48.web;

import com.cap48.db.DriftFinding;
import com.cap48.db.DriftFindingRepository;
import com.cap48.web.dto.DriftMatrixDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only view of the latest drift comparison run, as a 3x3 env-by-env matrix. */
@RestController
public class DriftController {

    private static final List<String> ENV_ORDER = List.of("dev", "test", "prod");

    private final DriftFindingRepository driftFindings;

    public DriftController(DriftFindingRepository driftFindings) {
        this.driftFindings = driftFindings;
    }

    @GetMapping("/api/drift-matrix")
    public DriftMatrixDto driftMatrix() {
        return driftFindings.findLatestComparedAt()
                .map(comparedAt -> {
                    List<DriftFinding> rows = driftFindings.findByComparedAt(comparedAt);
                    List<DriftMatrixDto.DriftPairDto> pairs = ENV_ORDER.stream()
                            .flatMap(a -> ENV_ORDER.stream().map(b -> pairFor(a, b, rows)))
                            .toList();
                    return new DriftMatrixDto(comparedAt, pairs);
                })
                .orElseGet(() -> new DriftMatrixDto(null, List.of()));
    }

    private DriftMatrixDto.DriftPairDto pairFor(String a, String b, List<DriftFinding> rows) {
        if (a.equals(b)) {
            return new DriftMatrixDto.DriftPairDto(a, b, false, 0);
        }
        long disallowedCount = rows.stream()
                .filter(d -> !d.isAllowed())
                .filter(d -> matchesPair(d, a, b))
                .count();
        return new DriftMatrixDto.DriftPairDto(a, b, disallowedCount > 0, disallowedCount);
    }

    private boolean matchesPair(DriftFinding d, String a, String b) {
        String envA = d.getEnvA().getName();
        String envB = d.getEnvB().getName();
        return (envA.equals(a) && envB.equals(b)) || (envA.equals(b) && envB.equals(a));
    }
}
