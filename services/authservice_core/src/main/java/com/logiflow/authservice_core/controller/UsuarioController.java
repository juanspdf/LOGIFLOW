package com.logiflow.authservice_core.controller;

import com.logiflow.authservice_core.dto.request.UpdateUserRequestDto;
import com.logiflow.authservice_core.dto.response.ApiResponseDto;
import com.logiflow.authservice_core.dto.response.UserResponseDto;
import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.RoleName;
import com.logiflow.authservice_core.model.enums.UserStatus;
import com.logiflow.authservice_core.service.UsuarioService;
import com.logiflow.authservice_core.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para la gestión de usuarios
 * Proporciona endpoints para CRUD y consultas de usuarios
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene un usuario por su ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR') or #id == authentication.principal.id")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Retorna la información completa de un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para acceder a este usuario")
    })
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUserById(
            @Parameter(description = "ID del usuario") @PathVariable UUID id
    ) {
        log.info("Solicitud para obtener usuario con ID: {}", id);
        UserResponseDto user = usuarioService.getUserById(id);
        return ResponseEntity.ok(ApiResponseDto.success(user, "Usuario obtenido exitosamente"));
    }

    /**
     * Obtiene un usuario por su email
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Obtener usuario por email",
            description = "Busca un usuario por su dirección de correo electrónico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUserByEmail(
            @Parameter(description = "Email del usuario") @PathVariable String email
    ) {
        log.info("Solicitud para obtener usuario con email: {}", email);
        UserResponseDto user = usuarioService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponseDto.success(user, "Usuario obtenido exitosamente"));
    }

    /**
     * Obtiene todos los usuarios activos
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Listar todos los usuarios activos",
            description = "Retorna la lista completa de usuarios activos en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente"
            )
    })
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllActiveUsers() {
        log.info("Solicitud para obtener todos los usuarios activos");
        List<UserResponseDto> users = usuarioService.getAllActiveUsers();
        return ResponseEntity.ok(ApiResponseDto.success(
                users,
                "Se encontraron " + users.size() + " usuarios activos"
        ));
    }

    /**
     * Obtiene usuarios por rol
     */
    @GetMapping("/rol/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Obtener usuarios por rol",
            description = "Retorna todos los usuarios que tienen un rol específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente"
            )
    })
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getUsersByRole(
            @Parameter(description = "Nombre del rol") @PathVariable RoleName roleName
    ) {
        log.info("Solicitud para obtener usuarios con rol: {}", roleName);
        List<UserResponseDto> users = usuarioService.getUsersByRole(roleName);
        return ResponseEntity.ok(ApiResponseDto.success(
                users,
                "Se encontraron " + users.size() + " usuarios con rol " + roleName
        ));
    }

    /**
     * Obtiene usuarios por zona
     */
    @GetMapping("/zona/{zoneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Obtener usuarios por zona",
            description = "Retorna todos los usuarios asignados a una zona específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente"
            )
    })
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getUsersByZone(
            @Parameter(description = "ID de la zona") @PathVariable String zoneId
    ) {
        log.info("Solicitud para obtener usuarios de la zona: {}", zoneId);
        List<UserResponseDto> users = usuarioService.getUsersByZone(zoneId);
        return ResponseEntity.ok(ApiResponseDto.success(
                users,
                "Se encontraron " + users.size() + " usuarios en la zona"
        ));
    }

    /**
     * Obtiene repartidores disponibles por zona y tipo de flota
     */
    @GetMapping("/repartidores/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Obtener repartidores disponibles",
            description = "Retorna repartidores disponibles filtrados por zona y tipo de flota"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de repartidores obtenida exitosamente"
            )
    })
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAvailableRepartidores(
            @Parameter(description = "ID de la zona") @RequestParam String zoneId,
            @Parameter(description = "Tipo de flota") @RequestParam FleetType fleetType
    ) {
        log.info("Solicitud para obtener repartidores disponibles - Zona: {}, Flota: {}", 
                zoneId, fleetType);
        List<UserResponseDto> repartidores = usuarioService.getAvailableRepartidores(zoneId, fleetType);
        return ResponseEntity.ok(ApiResponseDto.success(
                repartidores,
                "Se encontraron " + repartidores.size() + " repartidores disponibles"
        ));
    }

    /**
     * Busca usuarios por nombre o apellido
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Buscar usuarios",
            description = "Busca usuarios por nombre o apellido usando un término de búsqueda"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda completada exitosamente"
            )
    })
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> searchUsers(
            @Parameter(description = "Término de búsqueda") @RequestParam String searchTerm
    ) {
        log.info("Búsqueda de usuarios con término: {}", searchTerm);
        List<UserResponseDto> users = usuarioService.searchUsers(searchTerm);
        return ResponseEntity.ok(ApiResponseDto.success(
                users,
                "Se encontraron " + users.size() + " usuarios"
        ));
    }

    /**
     * Actualiza los datos de un usuario
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE') or #id == authentication.principal.id")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para actualizar este usuario")
    })
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        log.info("Solicitud para actualizar usuario con ID: {}", id);
        UserResponseDto updatedUser = usuarioService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponseDto.success(
                updatedUser,
                "Usuario actualizado exitosamente"
        ));
    }

    /**
     * Actualiza el estado de un usuario
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'SUPERVISOR')")
    @Operation(
            summary = "Actualizar estado de usuario",
            description = "Cambia el estado de un usuario (ACTIVE, INACTIVE, SUSPENDED, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUserStatus(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado") @RequestParam UserStatus status
    ) {
        log.info("Solicitud para actualizar estado del usuario {} a {}", id, status);
        UserResponseDto updatedUser = usuarioService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponseDto.success(
                updatedUser,
                "Estado del usuario actualizado exitosamente"
        ));
    }

    /**
     * Elimina un usuario (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(
            summary = "Eliminar usuario",
            description = "Realiza una eliminación lógica del usuario (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario eliminado exitosamente"
            ),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(
            @Parameter(description = "ID del usuario") @PathVariable UUID id
    ) {
        log.info("Solicitud para eliminar usuario con ID: {}", id);
        usuarioService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDto.success("Usuario eliminado exitosamente"));
    }

    /**
     * Obtiene estadísticas de usuarios por rol
     */
    @GetMapping("/estadisticas/por-rol")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(
            summary = "Obtener estadísticas por rol",
            description = "Retorna el conteo de usuarios para cada rol del sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente"
            )
    })
    public ResponseEntity<ApiResponseDto<Map<RoleName, Long>>> getUserStatisticsByRole() {
        log.info("Solicitud para obtener estadísticas de usuarios por rol");

        Map<RoleName, Long> statistics = Map.of(
                RoleName.CLIENTE, usuarioService.countUsersByRole(RoleName.CLIENTE),
                RoleName.REPARTIDOR, usuarioService.countUsersByRole(RoleName.REPARTIDOR),
                RoleName.SUPERVISOR, usuarioService.countUsersByRole(RoleName.SUPERVISOR),
                RoleName.GERENTE, usuarioService.countUsersByRole(RoleName.GERENTE),
                RoleName.ADMIN, usuarioService.countUsersByRole(RoleName.ADMIN)
        );

        return ResponseEntity.ok(ApiResponseDto.success(
                statistics,
                "Estadísticas obtenidas exitosamente"
        ));
    }
}
