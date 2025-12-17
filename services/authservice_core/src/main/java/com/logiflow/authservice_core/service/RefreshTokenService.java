package com.logiflow.authservice_core.service;

import com.logiflow.authservice_core.config.JwtConfig;
import com.logiflow.authservice_core.exception.UnauthorizedException;
import com.logiflow.authservice_core.model.entity.RefreshToken;
import com.logiflow.authservice_core.model.entity.Usuario;
import com.logiflow.authservice_core.repository.RefreshTokenRepository;
import com.logiflow.authservice_core.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio para la gestión de Refresh Tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    /**
     * Crea un nuevo refresh token para el usuario
     */
    @Transactional
    public RefreshToken createRefreshToken(Usuario usuario, String ipAddress, String userAgent) {
        // Calcular fecha de expiración
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshExpiration() / 1000);

        // Generar token único
        String token = UUID.randomUUID().toString();

        // Crear entidad
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .usuario(usuario)
                .expiryDate(expiryDate)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        @SuppressWarnings("null")
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        return saved;
    }

    /**
     * Valida y obtiene un refresh token
     */
    @Transactional(readOnly = true)
    public RefreshToken validateAndGetToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException(Constants.ERROR_TOKEN_INVALID));

        if (refreshToken.isDeleted()) {
            throw new UnauthorizedException(Constants.ERROR_TOKEN_INVALID);
        }

        if (refreshToken.isExpired()) {
            throw new UnauthorizedException("El token ha sido revocado");
        }

        if (refreshToken.isExpired()) {
            throw new UnauthorizedException(Constants.ERROR_TOKEN_EXPIRED);
        }

        return refreshToken;
    }

    /**
     * Revoca un refresh token específico
     */
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException(Constants.ERROR_TOKEN_INVALID));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revocado: {}", token.substring(0, 10) + "...");
    }

    /**
     * Revoca todos los tokens de un usuario
     */
    @Transactional
    public void revokeAllUserTokens(UUID usuarioId) {
        int revokedCount = refreshTokenRepository.revokeAllTokensByUsuarioId(usuarioId);
        log.info("Revocados {} tokens del usuario: {}", revokedCount, usuarioId);
    }

    /**
     * Elimina tokens expirados
     */
    @Transactional
    public int deleteExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Eliminados {} tokens expirados", deletedCount);
        return deletedCount;
    }

    /**
     * Cuenta tokens válidos de un usuario
     */
    @Transactional(readOnly = true)
    public long countValidUserTokens(UUID usuarioId) {
        return refreshTokenRepository.countValidTokensByUsuarioId(usuarioId);
    }
}
