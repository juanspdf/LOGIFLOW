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
@Table(name = "moto")
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("MOTO")
@PrimaryKeyJoinColumn(name = "vehiculo_id")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Moto extends Vehiculo {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MotoType tipo;

    @Column(nullable = false)
    @NotNull(message = "Debe especificar si tiene casco (true/false)")
    private Boolean tieneCasco;


}
