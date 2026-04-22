package com.byteentropy.sentinel_core.infra.repository;

import com.byteentropy.sentinel_core.domain.model.RiskResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskResultRepository extends JpaRepository<RiskResult, String> {
}