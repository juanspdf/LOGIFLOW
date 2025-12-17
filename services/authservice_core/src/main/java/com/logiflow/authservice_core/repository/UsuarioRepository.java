package com.logiflow.authservice_core.repository;

import com.logiflow.authservice_core.model.entity.Usuario;
import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.RoleName;
import com.logiflow.authservice_core.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca un usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por email y que no esté eliminado
     */
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<Usuario> findByEmailAndNotDeleted(@Param("email") String email);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado y que no esté eliminado
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u " +
            "WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    /**
     * Busca usuarios por rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.name = :roleName AND u.deletedAt IS NULL")
    List<Usuario> findByRoleName(@Param("roleName") RoleName roleName);

    /**
     * Busca usuarios por estado
     */
    @Query("SELECT u FROM Usuario u WHERE u.status = :status AND u.deletedAt IS NULL")
    List<Usuario> findByStatus(@Param("status") UserStatus status);

    /**
     * Busca usuarios por tipo de flota
     */
    @Query("SELECT u FROM Usuario u WHERE u.fleetType = :fleetType AND u.deletedAt IS NULL")
    List<Usuario> findByFleetType(@Param("fleetType") FleetType fleetType);

    /**
     * Busca usuarios por zona
     */
    @Query("SELECT u FROM Usuario u WHERE u.zoneId = :zoneId AND u.deletedAt IS NULL")
    List<Usuario> findByZoneId(@Param("zoneId") String zoneId);

    /**
     * Busca repartidores disponibles por zona y tipo de flota
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.name = 'REPARTIDOR' " +
            "AND u.zoneId = :zoneId " +
            "AND u.fleetType = :fleetType " +
            "AND u.status = 'ACTIVE' " +
            "AND u.deletedAt IS NULL")
    List<Usuario> findAvailableRepartidoresByZoneAndFleet(
            @Param("zoneId") String zoneId,
            @Param("fleetType") FleetType fleetType
    );

    /**
     * Obtiene todos los usuarios activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.status = 'ACTIVE' AND u.deletedAt IS NULL")
    List<Usuario> findAllActive();

    /**
     * Obtiene todos los usuarios no eliminados
     */
    @Query("SELECT u FROM Usuario u WHERE u.deletedAt IS NULL")
    List<Usuario> findAllNotDeleted();

    /**
     * Cuenta usuarios por rol
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.name = :roleName AND u.deletedAt IS NULL")
    long countByRoleName(@Param("roleName") RoleName roleName);

    /**
     * Busca usuarios con cuenta bloqueada
     */
    @Query("SELECT u FROM Usuario u WHERE u.accountLockedUntil IS NOT NULL " +
            "AND u.accountLockedUntil > CURRENT_TIMESTAMP " +
            "AND u.deletedAt IS NULL")
    List<Usuario> findLockedAccounts();

    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND u.deletedAt IS NULL")
    List<Usuario> searchByNombreOrApellido(@Param("searchTerm") String searchTerm);
}