package com.logiflow.pedido.dto;

import com.logiflow.pedido.model.TipoEntrega;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPedidoRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private UUID clienteId;

    @NotBlank(message = "La dirección de origen es obligatoria")
    @Size(max = 500)
    private String direccionOrigen;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 500)
    private String direccionDestino;

    @NotNull(message = "El tipo de entrega es obligatorio")
    private TipoEntrega tipoEntrega;

    @NotBlank(message = "El ID de zona es obligatorio")
    @Size(max = 50)
    private String zonaId;

    @NotNull(message = "La distancia estimada es obligatoria")
    @DecimalMin(value = "0.1", message = "La distancia debe ser mayor a 0")
    @DecimalMax(value = "5000.0", message = "La distancia no puede exceder 5000 km")
    private BigDecimal distanciaKm;

    @Size(max = 1000)
    private String notas;
}
