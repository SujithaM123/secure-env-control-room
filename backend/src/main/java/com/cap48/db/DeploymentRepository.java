package com.cap48.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {
    List<Deployment> findByEnvironmentIdOrderByDeployedAtDesc(Long environmentId);

    List<Deployment> findByImageVersion(String imageVersion);

    Optional<Deployment> findTopByEnvironmentIdOrderByDeployedAtDesc(Long environmentId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT d.imageVersion FROM Deployment d")
    List<String> findDistinctImageVersions();
}
