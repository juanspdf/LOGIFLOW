package com.logiflow.authservice_core.utils;


/**
 * Clase de constantes globales del sistema
 */
public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== API PATHS ====================
    public static final String API_VERSION = "/api/v1";
    public static final String AUTH_BASE_PATH = API_VERSION + "/auth";

    // ==================== ROLES ====================
    public static final String ROLE_CLIENTE = "CLIENTE";
    public static final String ROLE_REPARTIDOR = "REPARTIDOR";
    public static final String ROLE_SUPERVISOR = "SUPERVISOR";
    public static final String ROLE_GERENTE = "GERENTE";
    public static final String ROLE_ADMIN = "ADMIN";

    // ==================== JWT ====================
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "JWT";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SCOPE = "scope";
    public static final String CLAIM_ZONE_ID = "zone_id";
    public static final String CLAIM_FLEET_TYPE = "fleet_type";

    // ==================== VALIDATION MESSAGES ====================
    public static final String VALIDATION_EMAIL_REQUIRED = "El email es obligatorio";
    public static final String VALIDATION_EMAIL_INVALID = "El formato del email no es válido";
    public static final String VALIDATION_PASSWORD_REQUIRED = "La contraseña es obligatoria";
    public static final String VALIDATION_PASSWORD_SIZE = "La contraseña debe tener entre 8 y 100 caracteres";
    public static final String VALIDATION_NAME_REQUIRED = "El nombre es obligatorio";
    public static final String VALIDATION_LASTNAME_REQUIRED = "El apellido es obligatorio";
    public static final String VALIDATION_PHONE_INVALID = "El formato del teléfono no es válido";
    public static final String VALIDATION_ROLE_REQUIRED = "El rol es obligatorio";

    // ==================== ERROR MESSAGES ====================
    public static final String ERROR_USER_NOT_FOUND = "Usuario no encontrado";
    public static final String ERROR_EMAIL_ALREADY_EXISTS = "El email ya está registrado";
    public static final String ERROR_INVALID_CREDENTIALS = "Credenciales inválidas";
    public static final String ERROR_TOKEN_EXPIRED = "Token expirado";
    public static final String ERROR_TOKEN_INVALID = "Token inválido";
    public static final String ERROR_UNAUTHORIZED = "No autorizado";
    public static final String ERROR_FORBIDDEN = "Acceso denegado";
    public static final String ERROR_INTERNAL_SERVER = "Error interno del servidor";

    // ==================== SUCCESS MESSAGES ====================
    public static final String SUCCESS_USER_REGISTERED = "Usuario registrado exitosamente";
    public static final String SUCCESS_LOGIN = "Inicio de sesión exitoso";
    public static final String SUCCESS_LOGOUT = "Cierre de sesión exitoso";
    public static final String SUCCESS_TOKEN_REFRESHED = "Token renovado exitosamente";

    // ==================== REGEX PATTERNS ====================
    public static final String PHONE_PATTERN = "^\\+?[0-9]{10,15}$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$";

    // ==================== DATE FORMATS ====================
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_ZONE = "America/Guayaquil";
}
