package com.tickets.Tickets.service;

import com.tickets.Tickets.domain.*;
import com.tickets.Tickets.dto.request.TicketRequest;
import com.tickets.Tickets.dto.response.TicketResponse;
import com.tickets.Tickets.exception.BadRequestException;
import com.tickets.Tickets.exception.ResourceNotFoundException;
import com.tickets.Tickets.mapper.TicketMapper;
import com.tickets.Tickets.repository.EventoRepository;
import com.tickets.Tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> listarTodos() {
        return ticketRepository.findAll().stream()
                .map(TicketMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listarPorEvento(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId).stream()
                .map(TicketMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listarPorEstado(EstadoTicket estado) {
        return ticketRepository.findByEstado(estado).stream()
                .map(TicketMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse obtenerPorId(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id: " + id));
        return TicketMapper.INSTANCE.toResponse(ticket);
    }

    @Transactional
    public List<TicketResponse> crearTickets(TicketRequest request) {
        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + request.getEventoId()));

        long ticketsExistentes = ticketRepository.findByEventoId(evento.getId()).size();
        if (ticketsExistentes + request.getCantidad() > evento.getCapacidad()) {
            throw new BadRequestException("No hay capacidad suficiente para la cantidad solicitada");
        }

        List<Ticket> ticketsCreados = new ArrayList<>();
        for (int i = 0; i < request.getCantidad(); i++) {
            TipoTicket tipo = request.getTipo();
            String codigo = generarCodigo(tipo, evento.getId());

            Ticket ticket = Ticket.builder()
                    .codigo(codigo)
                    .tipo(tipo)
                    .precio(tipo.calcularPrecio(evento.getPrecioBase()))
                    .estado(EstadoTicket.DISPONIBLE)
                    .evento(evento)
                    .beneficios(tipo.getBeneficios())
                    .build();

            ticketsCreados.add(ticketRepository.save(ticket));
        }

        return ticketsCreados.stream()
                .map(TicketMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse cancelarTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id: " + id));

        if (ticket.getEstado() == EstadoTicket.VENDIDO) {
            throw new BadRequestException("No se puede cancelar un ticket ya vendido");
        }

        ticket.setEstado(EstadoTicket.CANCELADO);
        return TicketMapper.INSTANCE.toResponse(ticketRepository.save(ticket));
    }

    private String generarCodigo(TipoTicket tipo, Long eventoId) {
        return tipo.name().substring(0, 3) + "-" + eventoId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
