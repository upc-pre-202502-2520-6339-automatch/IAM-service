package com.iam.infrastructure.persistence.jpa.repositories;

import com.iam.domain.model.entities.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJtiHash(String jtiHash);
}
