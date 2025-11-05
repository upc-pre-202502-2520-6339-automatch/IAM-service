package com.iam.infrastructure.tokens.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import com.iam.application.internal.outboundservices.tokens.TokenService;

import java.time.Instant;

public interface BearerTokenService  extends TokenService {

    String getBearerTokenFrom(HttpServletRequest request);
    String generateToken(Authentication authentication);

    String getJti(String token);
    Instant getExpiration(String token);

}
