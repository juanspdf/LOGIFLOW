package com.logiflow.pedido.dto;

import com.logiflow.pedido.model.EstadoPedido;
import com.logiflow.pedido.model.TipoEntrega;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {

    private UUID id;
    private UUID clienteId;
    private String direccionOrigen;
    private String direccionDestino;
    private TipoEntrega tipoEntrega;
    private String zonaId;
    private BigDecimal distanciaKm;
    private String notas;
    private EstadoPedido estado;
    private UUID repartidorId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;
}
