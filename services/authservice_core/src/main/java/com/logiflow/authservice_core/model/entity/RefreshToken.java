package com.logiflow.authservice_core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad RefreshToken
 * Almacena tokens de refresco para renovación de JWT
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_usuario_id", columnList = "usuario_id")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
public class RefreshToken extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;


    // ==================== Business Methods ====================

    /**
     * Verifica si el token ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Verifica si el token es válido (no revocado y no expirado)
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    /**
     * Revoca el token
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Verifica si el token pertenece al usuario especificado
     */
    public boolean belongsToUser(UUID userId) {
        return usuario != null && usuario.getId().equals(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", token='" + token.substring(0, 20) + "...'" +
                ", expiryDate=" + expiryDate +
                ", revoked=" + revoked +
                '}';
    }
}
