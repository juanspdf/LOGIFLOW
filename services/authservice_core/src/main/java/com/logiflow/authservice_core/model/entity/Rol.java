package com.logiflow.authservice_core.model.entity;

import com.logiflow.authservice_core.model.enums.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Entidad Rol
 * Representa los diferentes roles de usuario en el sistema
 */
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
public class Rol extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private RoleName name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rol rol)) return false;
        return id != null && id.equals(rol.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Rol{" +
                "id=" + id +
                ", name=" + name +
                ", active=" + active +
                '}';
    }
}
