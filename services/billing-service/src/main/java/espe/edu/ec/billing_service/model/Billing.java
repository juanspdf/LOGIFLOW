package espe.edu.ec.billing_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El pedido es obligatorio")
    @Column(nullable = false)
    private Long pedidoId;

    @NotNull(message = "El cliente es obligatorio")
    @Column(nullable = false)
    private Long clienteId;

    @NotNull(message = "El subtotal es obligatorio")
    @Column(nullable = false)
    private BigDecimal subtotal;

    @NotNull(message = "Los impuestos son obligatorios")
    private BigDecimal impuestos;

    @NotNull(message = "El total es obligatorio")
    private BigDecimal total;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoType estado;

    @PrePersist
    public void prePersist() {
        this.fechaEmision = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoType.BORRADOR;
        }
    }
}
