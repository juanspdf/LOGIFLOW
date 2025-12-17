package com.logiflow.authservice_core.dto.mapper;

import com.logiflow.authservice_core.dto.request.RegisterRequestDto;
import com.logiflow.authservice_core.dto.request.UpdateUserRequestDto;
import com.logiflow.authservice_core.dto.response.UserResponseDto;
import com.logiflow.authservice_core.model.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para la entidad Usuario
 * Utiliza MapStruct para conversiones automáticas entre entidad y DTOs
 */
@Mapper(
        componentModel = "spring",
        uses = {RolMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UsuarioMapper {

    /**
     * Convierte Usuario a UserResponse
     */
    @Mapping(target = "nombreCompleto", expression = "java(usuario.getNombreCompleto())")
    UserResponseDto toUserResponse(Usuario usuario);

    /**
     * Convierte RegisterRequest a Usuario
     * No mapea el password (se manejará en el servicio con encriptación)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    Usuario toUsuario(RegisterRequestDto request);

    /**
     * Actualiza un Usuario existente con los datos de UpdateUserRequest
     * Solo actualiza los campos no nulos
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateUsuarioFromDto(UpdateUserRequestDto request, @MappingTarget Usuario usuario);
}
