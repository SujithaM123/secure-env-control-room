package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RotationEventRepository extends JpaRepository<RotationEvent, Long> {
    List<RotationEvent> findByEnvironmentIdOrderByStartedAtDesc(Long environmentId);

    Optional<RotationEvent> findTopByEnvironmentIdAndStatusOrderByCompletedAtDesc(Long environmentId, String status);

    List<RotationEvent> findByCommitSha(String commitSha);

    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT r.commitSha FROM RotationEvent r WHERE r.commitSha IS NOT NULL")
    List<String> findDistinctCommitShas();
}
