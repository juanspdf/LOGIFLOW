package com.logiflow.authservice_core.dto.request;

import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.UserStatus;
import com.logiflow.authservice_core.utils.Constants;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualización de datos de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDto {

    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellido;

    @Pattern(regexp = Constants.PHONE_PATTERN, message = Constants.VALIDATION_PHONE_INVALID)
    private String telefono;

    @Size(max = 255)
    private String direccion;

    private UserStatus status;

    private FleetType fleetType;

    private String zoneId;
}
