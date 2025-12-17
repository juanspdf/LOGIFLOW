package com.logiflow.pedido.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pedidos", indexes = {
        @Index(name = "idx_estado", columnList = "estado"),
        @Index(name = "idx_cliente_id", columnList = "cliente_id"),
        @Index(name = "idx_zona_id", columnList = "zona_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID id;

    @NotNull(message = "El ID del cliente es obligatorio")
    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @NotBlank(message = "La dirección de origen es obligatoria")
    @Size(max = 500)
    @Column(name = "direccion_origen", nullable = false, length = 500)
    private String direccionOrigen;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 500)
    @Column(name = "direccion_destino", nullable = false, length = 500)
    private String direccionDestino;

    @NotNull(message = "El tipo de entrega es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 30)
    private TipoEntrega tipoEntrega;

    @NotBlank(message = "El ID de zona es obligatorio")
    @Size(max = 50)
    @Column(name = "zona_id", nullable = false, length = 50)
    private String zonaId;

    @NotNull(message = "La distancia estimada es obligatoria")
    @Column(name = "distancia_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    @Size(max = 1000)
    @Column(name = "notas", length = 1000)
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.RECIBIDO;

    @Column(name = "repartidor_id")
    private UUID repartidorId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Size(max = 500)
    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPedido.RECIBIDO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Cancela el pedido con un motivo
     */
    public void cancelar(String motivo) {
        this.estado = EstadoPedido.CANCELADO;
        this.fechaCancelacion = LocalDateTime.now();
        this.motivoCancelacion = motivo;
    }

    /**
     * Verifica si el pedido puede ser cancelado
     */
    public boolean puedeCancelarse() {
        return this.estado == EstadoPedido.RECIBIDO || this.estado == EstadoPedido.ASIGNADO;
    }
}
