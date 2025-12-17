package com.logiflow.authservice_core.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Clase base para todas las entidades
 * Proporciona campos de auditoría comunes
 */
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la entidad como eliminada (soft delete)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la entidad está eliminada
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Restaura una entidad eliminada
     */
    public void restore() {
        this.deletedAt = null;
    }
}