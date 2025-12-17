package com.logiflow.pedido.dto;

import com.logiflow.pedido.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPedidoRequest {

    private String direccionDestino;
    
    private String notas;

    @NotNull(message = "El estado es obligatorio")
    private EstadoPedido estado;

    private UUID repartidorId;
}
