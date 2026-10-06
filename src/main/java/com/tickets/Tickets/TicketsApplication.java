package com.tickets.Tickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la plataforma de tickets.
 *
 * Activa el escaneo de componentes de com.tickets.Tickets, donde viven
 * controller, service, repository, domain, dto, mapper, security, exception
 * y bootstrap.
 */
@SpringBootApplication
public class TicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketsApplication.class, args);
    }
}