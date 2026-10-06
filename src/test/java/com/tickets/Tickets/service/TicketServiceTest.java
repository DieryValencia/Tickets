package com.tickets.Tickets.service;

import com.tickets.Tickets.domain.*;
import com.tickets.Tickets.dto.request.TicketRequest;
import com.tickets.Tickets.dto.response.TicketResponse;
import com.tickets.Tickets.exception.BadRequestException;
import com.tickets.Tickets.exception.ResourceNotFoundException;
import com.tickets.Tickets.mapper.TicketMapper;
import com.tickets.Tickets.repository.EventoRepository;
import com.tickets.Tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private TicketService ticketService;

    private Evento evento;
    private Ticket ticket;
    private TicketRequest ticketRequest;

    @BeforeEach
    void setUp() {
        Usuario organizador = Usuario.builder()
                .id(1L)
                .nombre("Carlos Organizador")
                .email("organizador@example.com")
                .password("encodedPassword")
                .rol(Rol.ORGANIZADOR)
                .build();

        evento = Evento.builder()
                .id(1L)
                .nombre("Concierto Rock")
                .descripcion("Gran concierto de rock")
                .fecha(LocalDateTime.now().plusDays(30))
                .lugar("Estadio Nacional")
                .capacidad(500)
                .precioBase(new BigDecimal("50.00"))
                .categoria("Musica")
                .organizador(organizador)
                .build();

        ticket = Ticket.builder()
                .id(1L)
                .codigo("GEN-1-ABC12345")
                .tipo(TipoTicket.GENERAL)
                .precio(new BigDecimal("50.00"))
                .estado(EstadoTicket.DISPONIBLE)
                .evento(evento)
                .beneficios("Acceso general al evento")
                .build();

        ticketRequest = new TicketRequest();
        ticketRequest.setTipo(TipoTicket.GENERAL);
        ticketRequest.setEventoId(1L);
        ticketRequest.setCantidad(5);
    }

    @Test
    @DisplayName("Listar todos los tickets - retorna lista de tickets")
    void listarTodos_Success() {
        Ticket ticket2 = Ticket.builder()
                .id(2L)
                .codigo("VIP-1-DEF67890")
                .tipo(TipoTicket.VIP)
                .precio(new BigDecimal("75.00"))
                .estado(EstadoTicket.DISPONIBLE)
                .evento(evento)
                .beneficios("Acceso preferente, zona VIP y meet & greet")
                .build();

        when(ticketRepository.findAll()).thenReturn(List.of(ticket, ticket2));

        List<TicketResponse> response = ticketService.listarTodos();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getCodigo()).isEqualTo("GEN-1-ABC12345");
        assertThat(response.get(1).getCodigo()).isEqualTo("VIP-1-DEF67890");
        verify(ticketRepository).findAll();
    }

    @Test
    @DisplayName("Listar tickets por evento - retorna tickets del evento")
    void listarPorEvento_Success() {
        when(ticketRepository.findByEventoId(1L)).thenReturn(List.of(ticket));

        List<TicketResponse> response = ticketService.listarPorEvento(1L);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getEventoNombre()).isEqualTo("Concierto Rock");
        verify(ticketRepository).findByEventoId(1L);
    }

    @Test
    @DisplayName("Listar tickets por estado - retorna tickets con el estado dado")
    void listarPorEstado_Success() {
        when(ticketRepository.findByEstado(EstadoTicket.DISPONIBLE)).thenReturn(List.of(ticket));

        List<TicketResponse> response = ticketService.listarPorEstado(EstadoTicket.DISPONIBLE);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getEstado()).isEqualTo(EstadoTicket.DISPONIBLE);
        verify(ticketRepository).findByEstado(EstadoTicket.DISPONIBLE);
    }

    @Test
    @DisplayName("Obtener ticket por ID - retorna ticket encontrado")
    void obtenerPorId_Success() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.obtenerPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCodigo()).isEqualTo("GEN-1-ABC12345");
        assertThat(response.getEstado()).isEqualTo(EstadoTicket.DISPONIBLE);
        verify(ticketRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener ticket por ID no encontrado - lanza ResourceNotFoundException")
    void obtenerPorId_NotFound_ThrowsResourceNotFoundException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket no encontrado con id: 999");

        verify(ticketRepository).findById(999L);
    }

    @Test
    @DisplayName("Crear tickets - exitoso")
    void crearTickets_Success() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(ticketRepository.findByEventoId(1L)).thenReturn(List.of());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        List<TicketResponse> response = ticketService.crearTickets(ticketRequest);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(5);
        assertThat(response.get(0).getEstado()).isEqualTo(EstadoTicket.DISPONIBLE);
        verify(eventoRepository).findById(1L);
        verify(ticketRepository, times(5)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Crear tickets con evento no encontrado - lanza ResourceNotFoundException")
    void crearTickets_EventoNotFound_ThrowsResourceNotFoundException() {
        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.crearTickets(ticketRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Evento no encontrado con id: 999");

        verify(eventoRepository).findById(999L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Crear tickets excediendo capacidad - lanza BadRequestException")
    void crearTickets_ExceedsCapacity_ThrowsBadRequestException() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(ticketRepository.findByEventoId(1L)).thenReturn(List.of(ticket, ticket, ticket, ticket, ticket));

        assertThatThrownBy(() -> ticketService.crearTickets(ticketRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No hay capacidad suficiente para la cantidad solicitada");

        verify(eventoRepository).findById(1L);
        verify(ticketRepository).findByEventoId(1L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Cancelar ticket disponible - exitoso")
    void cancelarTicket_Disponible_Success() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponse response = ticketService.cancelarTicket(1L);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoTicket.CANCELADO);
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Cancelar ticket vendido - lanza BadRequestException")
    void cancelarTicket_Vendido_ThrowsBadRequestException() {
        Ticket ticketVendido = Ticket.builder()
                .id(1L)
                .codigo("GEN-1-ABC12345")
                .tipo(TipoTicket.GENERAL)
                .precio(new BigDecimal("50.00"))
                .estado(EstadoTicket.VENDIDO)
                .evento(evento)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketVendido));

        assertThatThrownBy(() -> ticketService.cancelarTicket(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No se puede cancelar un ticket ya vendido");

        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Cancelar ticket no encontrado - lanza ResourceNotFoundException")
    void cancelarTicket_NotFound_ThrowsResourceNotFoundException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.cancelarTicket(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket no encontrado con id: 999");

        verify(ticketRepository).findById(999L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Cancelar ticket reservado - exitoso")
    void cancelarTicket_Reservado_Success() {
        Ticket ticketReservado = Ticket.builder()
                .id(1L)
                .codigo("GEN-1-ABC12345")
                .tipo(TipoTicket.GENERAL)
                .precio(new BigDecimal("50.00"))
                .estado(EstadoTicket.RESERVADO)
                .evento(evento)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketReservado));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketReservado);

        TicketResponse response = ticketService.cancelarTicket(1L);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoTicket.CANCELADO);
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
    }
}
