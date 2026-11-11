package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScanFindingRepository extends JpaRepository<ScanFinding, Long> {
    List<ScanFinding> findByCommitSha(String commitSha);

    List<ScanFinding> findAllByOrderByScannedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.commitSha FROM ScanFinding s")
    List<String> findDistinctCommitShas();
}
