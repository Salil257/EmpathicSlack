package com.empathic.proxy.repository;

import com.empathic.proxy.model.TeamInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamInstallationRepository extends JpaRepository<TeamInstallation, Long> {
    Optional<TeamInstallation> findByTeamId(String teamId);
}

