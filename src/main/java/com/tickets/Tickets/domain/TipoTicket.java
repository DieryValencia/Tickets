package com.tickets.Tickets.domain;

import java.math.BigDecimal;

public enum TipoTicket {
    GENERAL(new BigDecimal("1.0"), "Acceso general al evento"),
    VIP(new BigDecimal("1.5"), "Acceso preferente, zona VIP y meet & greet"),
    PLATINUM(new BigDecimal("2.0"), "Acceso total, lounge premium y estacionamiento"),
    ESTUDIANTE(new BigDecimal("0.7"), "Acceso con descuento estudiantil");

    private final BigDecimal multiplicadorPrecio;
    private final String beneficios;

    TipoTicket(BigDecimal multiplicadorPrecio, String beneficios) {
        this.multiplicadorPrecio = multiplicadorPrecio;
        this.beneficios = beneficios;
    }

    public BigDecimal getMultiplicadorPrecio() {
        return multiplicadorPrecio;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public BigDecimal calcularPrecio(BigDecimal precioBase) {
        return precioBase.multiply(multiplicadorPrecio);
    }
}
