package espe.edu.ec.fleet_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@Table(name = "camion")
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CAMION")
@PrimaryKeyJoinColumn(name = "vehiculo_id")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Camion extends Vehiculo {
    @Column(nullable = false)
    @NotNull(message = "Capacidad de carga es obligatoria")
    private Double capacidadCargaToneladas;

    @Column(nullable = false)
    @NotNull(message = "Número de ejes es obligatorio")
    private Integer numeroEjes;

    @Column(nullable = false)
    @NotNull(message = "Debe especificar si tiene rampa hidráulica")
    private Boolean tieneRampaHidraulica;
}
