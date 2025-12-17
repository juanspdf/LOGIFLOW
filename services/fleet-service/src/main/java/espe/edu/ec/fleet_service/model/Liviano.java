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
@Table(name = "liviano")
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("LIVIANO")
@PrimaryKeyJoinColumn(name = "vehiculo_id")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Liviano extends Vehiculo {
    @Column(nullable = false)
    @NotNull(message = "Número de puertas es obligatorio")
    private Integer numeroPuertas;

    @Column(nullable = false)
    @NotNull(message = "Debe especificar si tiene aire acondicionado")
    private Boolean tieneAireAcondicionado;
}
