package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DriftFindingRepository extends JpaRepository<DriftFinding, Long> {
    List<DriftFinding> findByAllowedFalseAndComparedAt(Instant comparedAt);

    List<DriftFinding> findByComparedAt(Instant comparedAt);

    List<DriftFinding> findByCommitSha(String commitSha);

    @Query("SELECT MAX(d.comparedAt) FROM DriftFinding d")
    Optional<Instant> findLatestComparedAt();

    @Query("SELECT DISTINCT d.commitSha FROM DriftFinding d WHERE d.commitSha IS NOT NULL")
    List<String> findDistinctCommitShas();
}
