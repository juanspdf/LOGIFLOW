package com.logiflow.pedido.controller;

import com.logiflow.pedido.dto.*;
import com.logiflow.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Gestión de pedidos de entrega")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(
            summary = "Crear nuevo pedido",
            description = "Crea un nuevo pedido de entrega con estado RECIBIDO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<com.logiflow.pedido.dto.ApiResponse<PedidoResponse>> crearPedido(
            @Valid @RequestBody CrearPedidoRequest request
    ) {
        log.info("POST /pedidos - Creando pedido");
        PedidoResponse response = pedidoService.crearPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.logiflow.pedido.dto.ApiResponse.success(
                        response,
                        "Pedido creado exitosamente"
                ));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener pedido por ID",
            description = "Obtiene los detalles de un pedido específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<com.logiflow.pedido.dto.ApiResponse<PedidoResponse>> obtenerPedido(
            @PathVariable UUID id
    ) {
        log.info("GET /pedidos/{} - Obteniendo pedido", id);
        PedidoResponse response = pedidoService.obtenerPedido(id);
        return ResponseEntity.ok(com.logiflow.pedido.dto.ApiResponse.success(
                response,
                "Pedido encontrado"
        ));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Actualizar pedido",
            description = "Actualiza parcialmente los datos de un pedido (estado, dirección destino, repartidor, notas)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o pedido no actualizable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<com.logiflow.pedido.dto.ApiResponse<PedidoResponse>> actualizarPedido(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarPedidoRequest request
    ) {
        log.info("PATCH /pedidos/{} - Actualizando pedido", id);
        PedidoResponse response = pedidoService.actualizarPedido(id, request);
        return ResponseEntity.ok(com.logiflow.pedido.dto.ApiResponse.success(
                response,
                "Pedido actualizado exitosamente"
        ));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar pedido",
            description = "Cancela un pedido si está en estado RECIBIDO o ASIGNADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido cancelado exitosamente",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El pedido no puede ser cancelado en su estado actual",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<com.logiflow.pedido.dto.ApiResponse<PedidoResponse>> cancelarPedido(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarPedidoRequest request
    ) {
        log.info("PATCH /pedidos/{}/cancelar - Cancelando pedido", id);
        PedidoResponse response = pedidoService.cancelarPedido(id, request.getMotivo());
        return ResponseEntity.ok(com.logiflow.pedido.dto.ApiResponse.success(
                response,
                "Pedido cancelado exitosamente"
        ));
    }
}
