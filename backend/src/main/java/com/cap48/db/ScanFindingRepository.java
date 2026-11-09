package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScanFindingRepository extends JpaRepository<ScanFinding, Long> {
    List<ScanFinding> findByCommitSha(String commitSha);
}
