package com.logiflow.authservice_core.repository;

import com.logiflow.authservice_core.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad RefreshToken
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Busca un token por su valor
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca un token válido por su valor
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > CURRENT_TIMESTAMP " +
            "AND rt.deletedAt IS NULL")
    Optional<RefreshToken> findValidToken(@Param("token") String token);

    /**
     * Busca todos los tokens de un usuario
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.usuario.id = :usuarioId " +
            "AND rt.deletedAt IS NULL")
    List<RefreshToken> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Busca tokens válidos de un usuario
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.usuario.id = :usuarioId " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > CURRENT_TIMESTAMP " +
            "AND rt.deletedAt IS NULL")
    List<RefreshToken> findValidTokensByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Verifica si existe un token válido
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt " +
            "WHERE rt.token = :token " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > CURRENT_TIMESTAMP " +
            "AND rt.deletedAt IS NULL")
    boolean existsValidToken(@Param("token") String token);

    /**
     * Revoca todos los tokens de un usuario
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.usuario.id = :usuarioId AND rt.revoked = false")
    int revokeAllTokensByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Revoca un token específico
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.token = :token")
    int revokeToken(@Param("token") String token);

    /**
     * Elimina tokens expirados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :date")
    int deleteExpiredTokens(@Param("date") LocalDateTime date);

    /**
     * Elimina todos los tokens de un usuario
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.usuario.id = :usuarioId")
    int deleteAllByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Cuenta tokens válidos de un usuario
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.usuario.id = :usuarioId " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > CURRENT_TIMESTAMP " +
            "AND rt.deletedAt IS NULL")
    long countValidTokensByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Busca tokens expirados
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < CURRENT_TIMESTAMP " +
            "AND rt.deletedAt IS NULL")
    List<RefreshToken> findExpiredTokens();

    /**
     * Busca tokens por IP
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.ipAddress = :ipAddress " +
            "AND rt.deletedAt IS NULL")
    List<RefreshToken> findByIpAddress(@Param("ipAddress") String ipAddress);
}