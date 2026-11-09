package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessAuditRepository extends JpaRepository<AccessAudit, Long> {
}
