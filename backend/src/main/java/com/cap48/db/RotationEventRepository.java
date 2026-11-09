package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RotationEventRepository extends JpaRepository<RotationEvent, Long> {
    List<RotationEvent> findByEnvironmentIdOrderByStartedAtDesc(Long environmentId);
}
