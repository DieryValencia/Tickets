package com.tickets.Tickets.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventoRequest {

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    private LocalDateTime fecha;

    @NotBlank
    private String lugar;

    @NotNull
    @Min(1)
    private int capacidad;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioBase;

    private String categoria;

    private String imagenUrl;
}
