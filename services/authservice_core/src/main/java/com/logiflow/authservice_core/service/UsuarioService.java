package com.logiflow.authservice_core.service;

import com.logiflow.authservice_core.dto.mapper.UsuarioMapper;
import com.logiflow.authservice_core.dto.request.UpdateUserRequestDto;
import com.logiflow.authservice_core.dto.response.UserResponseDto;
import com.logiflow.authservice_core.exception.ResourceNotFoundException;
import com.logiflow.authservice_core.model.entity.Usuario;
import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.RoleName;
import com.logiflow.authservice_core.model.enums.UserStatus;
import com.logiflow.authservice_core.repository.UsuarioRepository;
import com.logiflow.authservice_core.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    /**
     * Obtiene un usuario por su ID
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        log.debug("Obteniendo usuario con ID: {}", id);

        @SuppressWarnings("null")
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));

        if (usuario.isDeleted()) {
            throw new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND);
        }

        return usuarioMapper.toUserResponse(usuario);
    }

    /**
     * Obtiene un usuario por su email
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        log.debug("Obteniendo usuario con email: {}", email);

        Usuario usuario = usuarioRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));

        return usuarioMapper.toUserResponse(usuario);
    }

    /**
     * Obtiene todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        log.debug("Obteniendo todos los usuarios activos");

        return usuarioRepository.findAllActive().stream()
                .map(usuarioMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(RoleName roleName) {
        log.debug("Obteniendo usuarios con rol: {}", roleName);

        return usuarioRepository.findByRoleName(roleName).stream()
                .map(usuarioMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene usuarios por zona
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByZone(String zoneId) {
        log.debug("Obteniendo usuarios de la zona: {}", zoneId);

        return usuarioRepository.findByZoneId(zoneId).stream()
                .map(usuarioMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene repartidores disponibles por zona y tipo de flota
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAvailableRepartidores(String zoneId, FleetType fleetType) {
        log.debug("Obteniendo repartidores disponibles - Zona: {}, Flota: {}", zoneId, fleetType);

        return usuarioRepository.findAvailableRepartidoresByZoneAndFleet(zoneId, fleetType).stream()
                .map(usuarioMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un usuario
     */
    @Transactional
    public UserResponseDto updateUser(UUID id, UpdateUserRequestDto request) {
        log.info("Actualizando usuario con ID: {}", id);

        @SuppressWarnings("null")
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));

        if (usuario.isDeleted()) {
            throw new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND);
        }

        // Actualizar solo los campos no nulos
        usuarioMapper.updateUsuarioFromDto(request, usuario);

        usuario = usuarioRepository.save(usuario);

        log.info("Usuario actualizado exitosamente: {}", usuario.getEmail());

        return usuarioMapper.toUserResponse(usuario);
    }

    /**
     * Actualiza el estado de un usuario
     */
    @Transactional
    public UserResponseDto updateUserStatus(UUID id, UserStatus status) {
        log.info("Actualizando estado del usuario {} a {}", id, status);

        @SuppressWarnings("null")
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));

        if (usuario.isDeleted()) {
            throw new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND);
        }

        UpdateUserRequestDto request = UpdateUserRequestDto.builder()
                .status(status)
                .build();

        usuarioMapper.updateUsuarioFromDto(request, usuario);
        usuario = usuarioRepository.save(usuario);

        return usuarioMapper.toUserResponse(usuario);
    }

    /**
     * Elimina un usuario (soft delete)
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Eliminando usuario con ID: {}", id);

        @SuppressWarnings("null")
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ERROR_USER_NOT_FOUND));

        usuario.softDelete();
        usuarioRepository.save(usuario);

        log.info("Usuario eliminado exitosamente: {}", usuario.getEmail());
    }

    /**
     * Busca usuarios por nombre o apellido
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> searchUsers(String searchTerm) {
        log.debug("Buscando usuarios con término: {}", searchTerm);

        return usuarioRepository.searchByNombreOrApellido(searchTerm).stream()
                .map(usuarioMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta usuarios por rol
     */
    @Transactional(readOnly = true)
    public long countUsersByRole(RoleName roleName) {
        return usuarioRepository.countByRoleName(roleName);
    }
}
