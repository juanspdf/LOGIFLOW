package com.logiflow.authservice_core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de propiedades JWT
 * Lee las propiedades desde application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * Clave secreta para firmar los tokens
     */
    private String secret;

    /**
     * Tiempo de expiración del access token en milisegundos
     */
    private Long expiration;

    /**
     * Tiempo de expiración del refresh token en milisegundos
     */
    private Long refreshExpiration;

    /**
     * Emisor del token
     */
    private String issuer;
}