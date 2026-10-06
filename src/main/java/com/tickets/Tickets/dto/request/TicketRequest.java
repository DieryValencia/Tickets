package com.tickets.Tickets.dto.request;

import com.tickets.Tickets.domain.TipoTicket;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequest {

    @NotNull
    private TipoTicket tipo;

    @NotNull
    private Long eventoId;

    @NotNull
    @Min(1)
    private int cantidad;
}
