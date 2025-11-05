package com.iam.application.internal.commandservices;

import com.iam.domain.model.commands.RevokeTokenCommand;
import com.iam.domain.model.entities.RevokedToken;
import com.iam.infrastructure.persistence.jpa.repositories.RevokedTokenRepository;
import com.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
@Transactional
public class TokenRevocationCommandService {

    private final RevokedTokenRepository revokedRepo;
    private final BearerTokenService tokenService;

    public TokenRevocationCommandService(RevokedTokenRepository revokedRepo,
                                         BearerTokenService tokenService) {
        this.revokedRepo = revokedRepo;
        this.tokenService = tokenService;
    }

    public void handle(RevokeTokenCommand command) {
        var token = command.token();
        var jti = tokenService.getJti(token);
        if (jti == null || jti.isBlank()) return;

        var jtiHash = sha256Hex(jti);
        if (revokedRepo.existsByJtiHash(jtiHash)) return;

        Instant expiresAt = tokenService.getExpiration(token); // guarda hasta exp
        revokedRepo.save(new RevokedToken(jtiHash, expiresAt));
    }

    private String sha256Hex(String value) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash jti", e);
        }
    }
}
