package com.logiflow.authservice_core.service;

import com.logiflow.authservice_core.config.JwtConfig;
import com.logiflow.authservice_core.model.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para la gestión de JWT (JSON Web Tokens)
 * Genera, valida y extrae información de los tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtConfig jwtConfig;

    /**
     * Genera un token JWT para el usuario
     */
    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = buildClaims(usuario);
        return createToken(claims, usuario.getEmail());
    }

    /**
     * Genera un token JWT con claims personalizados
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Construye los claims del token basados en el usuario
     */
    private Map<String, Object> buildClaims(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId().toString());
        claims.put("role", usuario.getRol().getName().name());
        claims.put("scope", buildScope(usuario));

        if (usuario.getZoneId() != null) {
            claims.put("zone_id", usuario.getZoneId());
        }

        if (usuario.getFleetType() != null) {
            claims.put("fleet_type", usuario.getFleetType().name());
        }

        return claims;
    }

    /**
     * Construye el scope basado en el rol del usuario
     */
    private String buildScope(Usuario usuario) {
        return switch (usuario.getRol().getName()) {
            case ADMIN -> "admin:all";
            case GERENTE -> "manager:read manager:write";
            case SUPERVISOR -> "supervisor:read supervisor:write";
            case REPARTIDOR -> "delivery:read delivery:write";
            case CLIENTE -> "customer:read customer:write";
        };
    }

    /**
     * Crea el token JWT
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .issuer(jwtConfig.getIssuer())
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtiene la fecha de expiración del token
     */
    public LocalDateTime getExpirationDate() {
        Date expirationDate = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());
        return LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Extrae el username (email) del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida el token contra los detalles del usuario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Obtiene la clave de firma del JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el userId del token
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", String.class);
    }

    /**
     * Extrae el rol del token
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extrae la zona del token
     */
    public String extractZoneId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("zone_id", String.class);
    }
}