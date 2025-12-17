package com.logiflow.authservice_core.model.enums;


/**
 * Enumeración de roles del sistema LogiFlow
 * Define los diferentes niveles de acceso y permisos
 */
public enum RoleName {
    /**
     * Cliente - Usuario que solicita entregas
     */
    CLIENTE,

    /**
     * Repartidor - Conductor que realiza entregas
     * Puede ser motorizado, vehículo liviano o camión
     */
    REPARTIDOR,

    /**
     * Supervisor - Supervisa operaciones en una zona específica
     */
    SUPERVISOR,

    /**
     * Gerente - Gestiona múltiples zonas y toma decisiones estratégicas
     */
    GERENTE,

    /**
     * Administrador - Control total del sistema
     */
    ADMIN;

    /**
     * Verifica si el rol es de tipo operativo (puede realizar entregas)
     */
    public boolean isOperational() {
        return this == REPARTIDOR;
    }

    /**
     * Verifica si el rol tiene permisos de gestión
     */
    public boolean isManager() {
        return this == SUPERVISOR || this == GERENTE || this == ADMIN;
    }

    /**
     * Obtiene el nivel de autoridad del rol (más alto = más permisos)
     */
    public int getAuthorityLevel() {
        return switch (this) {
            case CLIENTE -> 1;
            case REPARTIDOR -> 2;
            case SUPERVISOR -> 3;
            case GERENTE -> 4;
            case ADMIN -> 5;
        };
    }
}