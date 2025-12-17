package com.logiflow.authservice_core.dto.mapper;

import com.logiflow.authservice_core.dto.response.RolResponseDto;
import com.logiflow.authservice_core.model.entity.Rol;
import org.mapstruct.Mapper;

/**
 * Mapper para la entidad Rol
 * Utiliza MapStruct para conversiones automáticas entre entidad y DTOs
 */
@Mapper(componentModel = "spring")
public interface RolMapper {

    /**
     * Convierte Rol a RolResponse
     */
    RolResponseDto toRolResponse(Rol rol);
}