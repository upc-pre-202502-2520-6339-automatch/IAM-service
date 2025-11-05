package com.iam.infrastructure.authorization.sfs.pipeline;


import com.iam.infrastructure.persistence.jpa.repositories.RevokedTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import com.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;
    private final RevokedTokenRepository revokedTokenRepository;


    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService  userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, RevokedTokenRepository revokedTokenRepository, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.revokedTokenRepository = revokedTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // Si ya hay Authentication, sigue
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = tokenService.getBearerTokenFrom(request);
            if (token != null && tokenService.validateToken(token)) {

                // 1) Bloquear si está revocado
                String jti = tokenService.getJti(token); // asegúrate de tener este método en TokenServiceImpl
                if (jti != null && !jti.isBlank()) {
                    String jtiHash = sha256Hex(jti);
                    if (jtiHash != null && revokedTokenRepository.existsByJtiHash(jtiHash)) {
                        LOGGER.debug("JWT revocado (jti hash encontrado). Respondiendo 401.");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"token revoked\"}");
                        response.getWriter().flush();
                        return; // NO continúes la cadena
                    }
                }

                // 2) Autenticar
                String username = tokenService.getUsernameFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(username);
                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request)
                );
                LOGGER.debug("Autenticación establecida para '{}'", username);
            } else {
                LOGGER.debug("Sin token o token inválido (se permite continuar para endpoints públicos).");
            }
        } catch (Exception e) {
            // Deja que el EntryPoint maneje si corresponde
            LOGGER.error("Error en filtro JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String sha256Hex(String value) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            LOGGER.error("No se pudo calcular SHA-256 del jti: {}", e.getMessage());
            return null;
        }
    }
}





