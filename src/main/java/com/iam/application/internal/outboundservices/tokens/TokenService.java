package com.iam.application.internal.outboundservices.tokens;

public interface TokenService {
    // Versión vieja (la puedes dejar por compatibilidad)
    String generateToken(String username);

    // 👉 Nueva sobrecarga con datos del usuario
    String generateToken(String username, Long userId, java.util.List<String> roles);

    String getUsernameFromToken(String token);
    boolean validateToken(String token);
}