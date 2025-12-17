package com.logiflow.authservice_core.dto.response;

import com.logiflow.authservice_core.model.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para la respuesta de rol
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolResponseDto {

    private UUID id;

    private RoleName name;

    private String description;

    private boolean active;
}
