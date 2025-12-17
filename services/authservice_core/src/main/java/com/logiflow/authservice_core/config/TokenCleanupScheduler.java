package com.logiflow.authservice_core.config;

import com.logiflow.authservice_core.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduler para limpieza automática de tokens expirados
 * Ejecuta periódicamente para mantener la base de datos limpia
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Limpia tokens expirados cada día a las 3:00 AM
     * Cron: segundos minutos horas día mes día-semana
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpieza de tokens expirados...");

        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = refreshTokenRepository.deleteExpiredTokens(now);

            log.info("Limpieza completada. Tokens eliminados: {}", deletedCount);
        } catch (Exception e) {
            log.error("Error durante la limpieza de tokens: {}", e.getMessage(), e);
        }
    }

    /**
     * Revoca tokens que están por expirar (opcional)
     * Ejecuta cada hora
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void revokeTokensAboutToExpire() {
        log.debug("Verificando tokens próximos a expirar...");

        try {
            // Lógica adicional si se requiere
            // Por ejemplo: notificar usuarios, generar métricas, etc.
        } catch (Exception e) {
            log.error("Error verificando tokens: {}", e.getMessage(), e);
        }
    }
}
