package com.ventas.eCommerce.security;

import com.ventas.eCommerce.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Emite y valida los JWT usados por la API REST ({@code /api/**}).
 */
@Service
public class JwtService {

    private static final String ISSUER = "ecommerce";

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(AppProperties properties) {
        AppProperties.Jwt jwt = properties.jwt();
        if (jwt == null || jwt.secret() == null || jwt.secret().isBlank()) {
            throw new IllegalStateException("Falta configurar app.jwt.secret (clave Base64 de al menos 256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwt.secret()));
        this.expiration = jwt.expiration();
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.email())
                .claim("uid", user.id())
                .claim("role", user.rol().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /** Devuelve el email (subject) si el token es válido y no expiró. */
    public Optional<String> validateAndGetSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
