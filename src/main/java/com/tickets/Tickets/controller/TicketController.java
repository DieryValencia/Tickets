package com.tickets.Tickets.controller;

import com.tickets.Tickets.domain.EstadoTicket;
import com.tickets.Tickets.dto.request.TicketRequest;
import com.tickets.Tickets.dto.response.MensajeResponse;
import com.tickets.Tickets.dto.response.TicketResponse;
import com.tickets.Tickets.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Gestión de tickets")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    @Operation(summary = "Listar tickets", description = "Obtiene todos los tickets")
    @ApiResponse(responseCode = "200", description = "Lista de tickets")
    public ResponseEntity<List<TicketResponse>> listarTodos() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    @GetMapping("/evento/{eventoId}")
    @Operation(summary = "Tickets por evento", description = "Filtra tickets por evento")
    @ApiResponse(responseCode = "200", description = "Tickets del evento")
    public ResponseEntity<List<TicketResponse>> listarPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(ticketService.listarPorEvento(eventoId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Tickets por estado", description = "Filtra tickets por estado")
    @ApiResponse(responseCode = "200", description = "Tickets con el estado dado")
    public ResponseEntity<List<TicketResponse>> listarPorEstado(@PathVariable EstadoTicket estado) {
        return ResponseEntity.ok(ticketService.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket", description = "Obtiene un ticket por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket encontrado"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<TicketResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    @Operation(summary = "Crear tickets", description = "Crea uno o más tickets para un evento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tickets creados"),
            @ApiResponse(responseCode = "400", description = "Capacidad excedida o datos inválidos")
    })
    public ResponseEntity<List<TicketResponse>> crearTickets(@Valid @RequestBody TicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.crearTickets(request));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    @Operation(summary = "Cancelar ticket", description = "Cancela un ticket disponible o reservado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket cancelado"),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar un ticket vendido")
    })
    public ResponseEntity<TicketResponse> cancelarTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.cancelarTicket(id));
    }
}
