package com.tickets.Tickets.controller;

import com.tickets.Tickets.dto.request.EventoRequest;
import com.tickets.Tickets.dto.response.EventoResponse;
import com.tickets.Tickets.dto.response.MensajeResponse;
import com.tickets.Tickets.service.EventoService;
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
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestión de eventos")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Obtiene todos los eventos disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de eventos")
    public ResponseEntity<List<EventoResponse>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Eventos por categoría", description = "Filtra eventos por categoría")
    @ApiResponse(responseCode = "200", description = "Eventos encontrados")
    public ResponseEntity<List<EventoResponse>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(eventoService.listarPorCategoria(categoria));
    }

    @GetMapping("/proximos")
    @Operation(summary = "Próximos eventos", description = "Eventos con fecha futura")
    @ApiResponse(responseCode = "200", description = "Próximos eventos")
    public ResponseEntity<List<EventoResponse>> listarProximos() {
        return ResponseEntity.ok(eventoService.listarProximos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento", description = "Obtiene un evento por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    @Operation(summary = "Crear evento", description = "Crea un nuevo evento (solo ADMIN/ORGANIZADOR)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento creado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    public ResponseEntity<EventoResponse> crear(@Valid @RequestBody EventoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    @Operation(summary = "Actualizar evento", description = "Actualiza un evento existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento actualizado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest request) {
        return ResponseEntity.ok(eventoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar evento", description = "Elimina un evento (solo ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento eliminado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id) {
        eventoService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Evento eliminado exitosamente"));
    }
}
