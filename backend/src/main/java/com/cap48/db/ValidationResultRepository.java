package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ValidationResultRepository extends JpaRepository<ValidationResult, Long> {
    List<ValidationResult> findByEnvironmentIdOrderByCheckedAtDesc(Long environmentId);

    Optional<ValidationResult> findTopByEnvironmentIdOrderByCheckedAtDesc(Long environmentId);

    List<ValidationResult> findByCommitSha(String commitSha);

    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT v.commitSha FROM ValidationResult v WHERE v.commitSha IS NOT NULL")
    List<String> findDistinctCommitShas();
}
