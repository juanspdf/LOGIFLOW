package com.logiflow.pedido.service;

import com.logiflow.pedido.dto.ActualizarPedidoRequest;
import com.logiflow.pedido.dto.CrearPedidoRequest;
import com.logiflow.pedido.dto.PedidoResponse;
import com.logiflow.pedido.exception.BadRequestException;
import com.logiflow.pedido.exception.ResourceNotFoundException;
import com.logiflow.pedido.model.EstadoPedido;
import com.logiflow.pedido.model.Pedido;
import com.logiflow.pedido.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    @Transactional
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        log.info("Creando pedido para cliente: {}", request.getClienteId());

        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .direccionOrigen(request.getDireccionOrigen())
                .direccionDestino(request.getDireccionDestino())
                .tipoEntrega(request.getTipoEntrega())
                .zonaId(request.getZonaId())
                .distanciaKm(request.getDistanciaKm())
                .notas(request.getNotas())
                .estado(EstadoPedido.RECIBIDO)
                .build();

        @SuppressWarnings("null")
        Pedido savedPedido = pedidoRepository.save(pedido);
        log.info("Pedido creado exitosamente con ID: {}", savedPedido.getId());

        return mapToResponse(savedPedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(UUID id) {
        log.info("Obteniendo pedido con ID: {}", id);
        
        @SuppressWarnings("null")
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));

        return mapToResponse(pedido);
    }

    @Transactional
    public PedidoResponse actualizarPedido(UUID id, ActualizarPedidoRequest request) {
        log.info("Actualizando pedido con ID: {}", id);

        @SuppressWarnings("null")
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BadRequestException("No se puede actualizar un pedido cancelado");
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new BadRequestException("No se puede actualizar un pedido ya entregado");
        }

        if (request.getDireccionDestino() != null) {
            pedido.setDireccionDestino(request.getDireccionDestino());
        }

        if (request.getNotas() != null) {
            pedido.setNotas(request.getNotas());
        }

        if (request.getEstado() != null) {
            pedido.setEstado(request.getEstado());
        }

        if (request.getRepartidorId() != null) {
            pedido.setRepartidorId(request.getRepartidorId());
        }

        Pedido updatedPedido = pedidoRepository.save(pedido);
        log.info("Pedido actualizado exitosamente: {}", updatedPedido.getId());

        return mapToResponse(updatedPedido);
    }

    @Transactional
    public PedidoResponse cancelarPedido(UUID id, String motivo) {
        log.info("Cancelando pedido con ID: {}", id);

        @SuppressWarnings("null")
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));

        if (!pedido.puedeCancelarse()) {
            throw new BadRequestException(
                "No se puede cancelar un pedido en estado: " + pedido.getEstado()
            );
        }

        pedido.cancelar(motivo);
        Pedido canceledPedido = pedidoRepository.save(pedido);
        log.info("Pedido cancelado exitosamente: {}", canceledPedido.getId());

        return mapToResponse(canceledPedido);
    }

    private PedidoResponse mapToResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .direccionOrigen(pedido.getDireccionOrigen())
                .direccionDestino(pedido.getDireccionDestino())
                .tipoEntrega(pedido.getTipoEntrega())
                .zonaId(pedido.getZonaId())
                .distanciaKm(pedido.getDistanciaKm())
                .notas(pedido.getNotas())
                .estado(pedido.getEstado())
                .repartidorId(pedido.getRepartidorId())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .fechaCancelacion(pedido.getFechaCancelacion())
                .motivoCancelacion(pedido.getMotivoCancelacion())
                .build();
    }
}
