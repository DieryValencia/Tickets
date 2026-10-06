package com.tickets.Tickets.dto.response;

import com.tickets.Tickets.domain.EstadoTicket;
import com.tickets.Tickets.domain.TipoTicket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String codigo;
    private TipoTicket tipo;
    private BigDecimal precio;
    private EstadoTicket estado;
    private String eventoNombre;
    private String beneficios;
}
