package com.tickets.Tickets.controller;

import com.tickets.Tickets.dto.request.OrdenRequest;
import com.tickets.Tickets.dto.response.MensajeResponse;
import com.tickets.Tickets.dto.response.OrdenResponse;
import com.tickets.Tickets.service.OrdenService;
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
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Gestión de órdenes de compra")
public class OrdenController {

    private final OrdenService ordenService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    @Operation(summary = "Listar órdenes", description = "Obtiene todas las órdenes")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes")
    public ResponseEntity<List<OrdenResponse>> listarTodas() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Órdenes por usuario", description = "Obtiene las órdenes de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Órdenes del usuario"),
            @ApiResponse(responseCode = "403", description = "Solo puede ver sus propias órdenes")
    })
    public ResponseEntity<List<OrdenResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ordenService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden", description = "Obtiene una orden por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrdenResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear orden", description = "Compra tickets (solo CLIENTE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Tickets no disponibles"),
            @ApiResponse(responseCode = "403", description = "Solo clientes pueden comprar")
    })
    public ResponseEntity<OrdenResponse> crearOrden(@Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crearOrden(request));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar orden", description = "Cancela una orden y libera los tickets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden cancelada"),
            @ApiResponse(responseCode = "400", description = "La orden ya está cancelada")
    })
    public ResponseEntity<OrdenResponse> cancelarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.cancelarOrden(id));
    }
}
