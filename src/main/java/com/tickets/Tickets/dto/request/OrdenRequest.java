package com.tickets.Tickets.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrdenRequest {

    @NotEmpty
    private List<Long> ticketIds;

    @NotNull
    private String metodoPago;
}
