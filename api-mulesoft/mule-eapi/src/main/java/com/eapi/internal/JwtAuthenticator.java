package com.eapi.internal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtAuthenticator {

    public static Map<String, Object> authenticate(String token, String secret) throws Exception {
        try {
            // Remove o prefixo "Bearer " se existir
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            // Validação da Assinatura e Expiração
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            if (claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date())) {
                throw new Exception("Token expirado");
            }
            return Map.of(
                "userId", claims.get("user_id", String.class),
                "email", claims.get("email", String.class)
            );
        } catch (Exception e) {
            throw new Exception("Falha na autenticação Java: " + e.getMessage());
        }
    }
}