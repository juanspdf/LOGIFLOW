package com.logiflow.authservice_core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para la respuesta de datos de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID id;

    private String email;

    private String nombre;

    private String apellido;

    private String nombreCompleto;

    private String telefono;

    private String direccion;

    private RolResponseDto rol;

    private UserStatus status;

    private FleetType fleetType;

    private String zoneId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}