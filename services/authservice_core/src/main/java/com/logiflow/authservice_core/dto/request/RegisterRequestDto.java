package com.logiflow.authservice_core.dto.request;

import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.RoleName;
import com.logiflow.authservice_core.utils.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de registro de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    @NotBlank(message = Constants.VALIDATION_EMAIL_REQUIRED)
    @Email(message = Constants.VALIDATION_EMAIL_INVALID)
    private String email;

    @NotBlank(message = Constants.VALIDATION_PASSWORD_REQUIRED)
    @Size(min = 8, max = 100, message = Constants.VALIDATION_PASSWORD_SIZE)
    private String password;

    @NotBlank(message = Constants.VALIDATION_NAME_REQUIRED)
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = Constants.VALIDATION_LASTNAME_REQUIRED)
    @Size(max = 100)
    private String apellido;

    @Pattern(regexp = Constants.PHONE_PATTERN, message = Constants.VALIDATION_PHONE_INVALID)
    private String telefono;

    @Size(max = 255)
    private String direccion;

    @NotNull(message = Constants.VALIDATION_ROLE_REQUIRED)
    private RoleName roleName;

    // Campos específicos para repartidores
    private FleetType fleetType;

    private String zoneId;
}