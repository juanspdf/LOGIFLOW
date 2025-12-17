package com.logiflow.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelarPedidoRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(max = 500)
    private String motivo;
}
