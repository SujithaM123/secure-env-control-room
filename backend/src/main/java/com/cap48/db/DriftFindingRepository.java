package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface DriftFindingRepository extends JpaRepository<DriftFinding, Long> {
    List<DriftFinding> findByAllowedFalseAndComparedAt(Instant comparedAt);
}
