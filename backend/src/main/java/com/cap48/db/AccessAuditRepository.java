package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessAuditRepository extends JpaRepository<AccessAudit, Long> {
    List<AccessAudit> findByEnvironmentIdOrderByCapturedAtDescIdDesc(Long environmentId);
}
