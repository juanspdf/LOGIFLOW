package com.logiflow.authservice_core.model.enums;

/**
 * Estado del usuario en el sistema
 */
public enum UserStatus {
    /**
     * Usuario activo - Puede usar el sistema normalmente
     */
    ACTIVE,

    /**
     * Usuario inactivo - Suspendido temporalmente
     */
    INACTIVE,

    /**
     * Usuario bloqueado - No puede acceder al sistema
     */
    BLOCKED,

    /**
     * Usuario pendiente de verificación
     */
    PENDING_VERIFICATION;

    /**
     * Verifica si el usuario puede autenticarse
     */
    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}