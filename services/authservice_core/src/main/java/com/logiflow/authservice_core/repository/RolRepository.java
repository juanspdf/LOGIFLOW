package com.logiflow.authservice_core.repository;

import com.logiflow.authservice_core.model.entity.Rol;
import com.logiflow.authservice_core.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Rol
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

    /**
     * Busca un rol por su nombre
     */
    Optional<Rol> findByName(RoleName name);

    /**
     * Verifica si existe un rol con el nombre dado
     */
    boolean existsByName(RoleName name);

    /**
     * Obtiene todos los roles activos
     */
    @Query("SELECT r FROM Rol r WHERE r.active = true AND r.deletedAt IS NULL")
    List<Rol> findAllActive();

    /**
     * Obtiene todos los roles que no están eliminados
     */
    @Query("SELECT r FROM Rol r WHERE r.deletedAt IS NULL")
    List<Rol> findAllNotDeleted();

    /**
     * Busca roles por estado activo
     */
    List<Rol> findByActive(boolean active);
}
