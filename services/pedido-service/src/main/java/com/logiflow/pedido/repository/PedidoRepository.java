package com.logiflow.pedido.repository;

import com.logiflow.pedido.model.EstadoPedido;
import com.logiflow.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    
    List<Pedido> findByClienteId(UUID clienteId);
    
    List<Pedido> findByEstado(EstadoPedido estado);
    
    List<Pedido> findByZonaId(String zonaId);
    
    List<Pedido> findByRepartidorId(UUID repartidorId);
}
