package com.tickets.Tickets.service;

import com.tickets.Tickets.domain.*;
import com.tickets.Tickets.dto.request.OrdenRequest;
import com.tickets.Tickets.dto.response.OrdenResponse;
import com.tickets.Tickets.exception.BadRequestException;
import com.tickets.Tickets.exception.ResourceNotFoundException;
import com.tickets.Tickets.exception.UnauthorizedException;
import com.tickets.Tickets.mapper.OrdenMapper;
import com.tickets.Tickets.repository.OrdenRepository;
import com.tickets.Tickets.repository.TicketRepository;
import com.tickets.Tickets.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAll().stream()
                .map(OrdenMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorUsuario(Long usuarioId) {
        return ordenRepository.findByUsuarioId(usuarioId).stream()
                .map(OrdenMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtenerPorId(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con id: " + id));
        return OrdenMapper.INSTANCE.toResponse(orden);
    }

    @Transactional
    public OrdenResponse crearOrden(OrdenRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() != Rol.CLIENTE) {
            throw new UnauthorizedException("Solo los clientes pueden comprar tickets");
        }

        List<Ticket> tickets = ticketRepository.findAllById(request.getTicketIds());

        if (tickets.size() != request.getTicketIds().size()) {
            throw new BadRequestException("Algunos tickets no existen");
        }

        for (Ticket ticket : tickets) {
            if (ticket.getEstado() != EstadoTicket.DISPONIBLE) {
                throw new BadRequestException("El ticket " + ticket.getCodigo() + " no está disponible");
            }
        }

        BigDecimal total = tickets.stream()
                .map(Ticket::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Orden orden = Orden.builder()
                .usuario(usuario)
                .estado(EstadoOrden.COMPLETADA)
                .total(total)
                .metodoPago(request.getMetodoPago())
                .build();

        orden = ordenRepository.save(orden);

        for (Ticket ticket : tickets) {
            ticket.setEstado(EstadoTicket.VENDIDO);
            ticket.setOrden(orden);
            ticketRepository.save(ticket);
        }

        orden.setTickets(tickets);
        return OrdenMapper.INSTANCE.toResponse(orden);
    }

    @Transactional
    public OrdenResponse cancelarOrden(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con id: " + id));

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new BadRequestException("La orden ya está cancelada");
        }

        orden.setEstado(EstadoOrden.CANCELADA);

        for (Ticket ticket : orden.getTickets()) {
            ticket.setEstado(EstadoTicket.CANCELADO);
            ticket.setOrden(null);
            ticketRepository.save(ticket);
        }

        return OrdenMapper.INSTANCE.toResponse(ordenRepository.save(orden));
    }
}
