package com.iam.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "revoked_tokens",
        indexes = {
                @Index(name = "idx_revoked_tokens_jti_hash", columnList = "jti_hash", unique = true)
        }
)
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti_hash", nullable = false, unique = true, length = 64)
    private String jtiHash; // SHA-256 hex

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public RevokedToken(String jtiHash, Instant expiresAt) {
        this.jtiHash = jtiHash;
        this.expiresAt = expiresAt;
    }
}
