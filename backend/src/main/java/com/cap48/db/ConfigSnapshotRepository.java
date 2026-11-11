package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfigSnapshotRepository extends JpaRepository<ConfigSnapshot, Long> {
    Optional<ConfigSnapshot> findTopByEnvironmentIdOrderByCapturedAtDesc(Long environmentId);
}
