package com.tickets.Tickets.dto.response;

import com.tickets.Tickets.domain.EstadoOrden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenResponse {
    private Long id;
    private UsuarioResponse usuario;
    private List<TicketResponse> tickets;
    private LocalDateTime fechaCreacion;
    private EstadoOrden estado;
    private BigDecimal total;
    private String metodoPago;
}
