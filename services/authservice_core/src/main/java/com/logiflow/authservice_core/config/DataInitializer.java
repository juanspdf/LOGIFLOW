package com.logiflow.authservice_core.config;

import com.logiflow.authservice_core.model.entity.Rol;
import com.logiflow.authservice_core.model.enums.RoleName;
import com.logiflow.authservice_core.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Inicializador de datos
 * Carga datos iniciales en la base de datos al iniciar la aplicación
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        initializeRoles();
        log.info("Datos iniciales cargados correctamente");
    }

    /**
     * Inicializa los roles del sistema si no existen
     */
    private void initializeRoles() {
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (!rolRepository.existsByName(roleName)) {
                Rol rol = Rol.builder()
                        .name(roleName)
                        .description(getRoleDescription(roleName))
                        .active(true)
                        .build();

                @SuppressWarnings("null")
                Rol savedRol = rolRepository.save(rol);
                log.info("Rol creado: {}", savedRol.getName());
            }
        });
    }

    /**
     * Obtiene la descripción del rol
     */
    private String getRoleDescription(RoleName roleName) {
        return switch (roleName) {
            case CLIENTE -> "Usuario que solicita entregas";
            case REPARTIDOR -> "Conductor que realiza entregas";
            case SUPERVISOR -> "Supervisa operaciones en una zona específica";
            case GERENTE -> "Gestiona múltiples zonas y toma decisiones estratégicas";
            case ADMIN -> "Administrador del sistema con control total";
        };
    }
}
