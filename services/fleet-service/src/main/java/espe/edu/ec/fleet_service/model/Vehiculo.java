package espe.edu.ec.fleet_service.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "vehiculo")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_vehiculo", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipoVehiculo", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Moto.class, name = "MOTO"),
        @JsonSubTypes.Type(value = Liviano.class, name = "LIVIANO"),
        @JsonSubTypes.Type(value = Camion.class, name = "CAMION")
})
public abstract class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "El cilindraje es obligatorio")
    @Max(value = 10000, message = "El cilindraje incorrecto máx 10000")
    private Integer cilindraje;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z]{3}-\\d{3,4}$", message = "La placa debe tener formato AAA-123 o AAA-1234")
    private String placa;

    @Column(nullable = false)
    @NotBlank(message = "La marca es obligatoria")
    @Size(min = 2, max = 50, message = "La marca debe tener entre 2 y 50 caracteres")
    private String marca;

    @Column(nullable = false)
    @NotBlank(message = "El color es obligatorio")
    private String color;

    @Column(nullable = false)
    @NotBlank(message = "El modelo es obligatorio")
    @Size(min = 2, max = 50, message = "El modelo debe tener entre 2 y 50 caracteres")
    private String modelo;

    @Column(nullable = false)
    @NotBlank(message = "El año de fabricación es obligatorio")
    @Pattern(regexp = "^(19|20)\\d{2}$", message = "El año debe ser válido (ej: 1999, 2024)")
    private String anioFabricacion;

    @Column(nullable = false)
    @NotNull(message = "Debe especificar si esta activo (true/false)")
    private boolean activo;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado;

    @Column(name = "tipo_vehiculo", insertable = false, updatable = false)
    private String tipoVehiculo;

    @PrePersist
    protected void onCreate(){
        this.fechaCreacion = LocalDateTime.now();
        this.activo = false;
    }
}
