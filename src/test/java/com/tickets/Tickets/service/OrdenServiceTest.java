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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OrdenService ordenService;

    private Usuario cliente;
    private Usuario admin;
    private Evento evento;
    private Ticket ticket;
    private Orden orden;
    private OrdenRequest ordenRequest;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder()
                .id(1L)
                .nombre("María Cliente")
                .email("cliente@example.com")
                .password("encodedPassword")
                .rol(Rol.CLIENTE)
                .build();

        admin = Usuario.builder()
                .id(2L)
                .nombre("Admin Principal")
                .email("admin@example.com")
                .password("encodedPassword")
                .rol(Rol.ADMIN)
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
                .organizador(admin)
                .build();

        ticket = Ticket.builder()
                .id(1L)
                .codigo("GEN-1-ABC12345")
                .tipo(TipoTicket.GENERAL)
                .precio(new BigDecimal("50.00"))
                .estado(EstadoTicket.DISPONIBLE)
                .evento(evento)
                .build();

        orden = Orden.builder()
                .id(1L)
                .usuario(cliente)
                .estado(EstadoOrden.COMPLETADA)
                .total(new BigDecimal("50.00"))
                .metodoPago("Tarjeta de crédito")
                .build();

        ordenRequest = new OrdenRequest();
        ordenRequest.setTicketIds(List.of(1L));
        ordenRequest.setMetodoPago("Tarjeta de crédito");
    }

    private void setAuthentication(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Listar todas las órdenes - retorna lista de órdenes")
    void listarTodas_Success() {
        Orden orden2 = Orden.builder()
                .id(2L)
                .usuario(cliente)
                .estado(EstadoOrden.COMPLETADA)
                .total(new BigDecimal("100.00"))
                .metodoPago("PayPal")
                .build();

        when(ordenRepository.findAll()).thenReturn(List.of(orden, orden2));

        List<OrdenResponse> response = ordenService.listarTodas();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(response.get(1).getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        verify(ordenRepository).findAll();
    }

    @Test
    @DisplayName("Listar órdenes por usuario - retorna órdenes del usuario")
    void listarPorUsuario_Success() {
        when(ordenRepository.findByUsuarioId(1L)).thenReturn(List.of(orden));

        List<OrdenResponse> response = ordenService.listarPorUsuario(1L);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getUsuario().getEmail()).isEqualTo("cliente@example.com");
        verify(ordenRepository).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("Obtener orden por ID - retorna orden encontrada")
    void obtenerPorId_Success() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        OrdenResponse response = ordenService.obtenerPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(response.getTotal()).isEqualTo(new BigDecimal("50.00"));
        verify(ordenRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener orden por ID no encontrada - lanza ResourceNotFoundException")
    void obtenerPorId_NotFound_ThrowsResourceNotFoundException() {
        when(ordenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orden no encontrada con id: 999");

        verify(ordenRepository).findById(999L);
    }

    @Test
    @DisplayName("Crear orden como CLIENTE - exitoso")
    void crearOrden_AsCliente_Success() {
        setAuthentication("cliente@example.com");
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(ticketRepository.findAllById(List.of(1L))).thenReturn(List.of(ticket));
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        OrdenResponse response = ordenService.crearOrden(ordenRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(response.getTotal()).isEqualTo(new BigDecimal("50.00"));
        assertThat(response.getMetodoPago()).isEqualTo("Tarjeta de crédito");
        verify(usuarioRepository).findByEmail("cliente@example.com");
        verify(ticketRepository).findAllById(List.of(1L));
        verify(ordenRepository).save(any(Orden.class));
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Crear orden como ADMIN - lanza UnauthorizedException")
    void crearOrden_AsAdmin_ThrowsUnauthorizedException() {
        setAuthentication("admin@example.com");
        when(usuarioRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> ordenService.crearOrden(ordenRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Solo los clientes pueden comprar tickets");

        verify(usuarioRepository).findByEmail("admin@example.com");
        verify(ticketRepository, never()).findAllById(any());
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Crear orden con tickets no disponibles - lanza BadRequestException")
    void crearOrden_TicketNotAvailable_ThrowsBadRequestException() {
        setAuthentication("cliente@example.com");
        Ticket ticketVendido = Ticket.builder()
                .id(1L)
                .codigo("GEN-1-ABC12345")
                .tipo(TipoTicket.GENERAL)
                .precio(new BigDecimal("50.00"))
                .estado(EstadoTicket.VENDIDO)
                .evento(evento)
                .build();

        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(ticketRepository.findAllById(List.of(1L))).thenReturn(List.of(ticketVendido));

        assertThatThrownBy(() -> ordenService.crearOrden(ordenRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no está disponible");

        verify(usuarioRepository).findByEmail("cliente@example.com");
        verify(ticketRepository).findAllById(List.of(1L));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Crear orden con tickets inexistentes - lanza BadRequestException")
    void crearOrden_TicketsNotExist_ThrowsBadRequestException() {
        setAuthentication("cliente@example.com");
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));
        when(ticketRepository.findAllById(List.of(1L))).thenReturn(List.of());

        assertThatThrownBy(() -> ordenService.crearOrden(ordenRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Algunos tickets no existen");

        verify(usuarioRepository).findByEmail("cliente@example.com");
        verify(ticketRepository).findAllById(List.of(1L));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Cancelar orden - exitoso")
    void cancelarOrden_Success() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);

        OrdenResponse response = ordenService.cancelarOrden(1L);

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoOrden.CANCELADA);
        verify(ordenRepository).findById(1L);
        verify(ordenRepository).save(any(Orden.class));
    }

    @Test
    @DisplayName("Cancelar orden ya cancelada - lanza BadRequestException")
    void cancelarOrden_AlreadyCancelled_ThrowsBadRequestException() {
        Orden ordenCancelada = Orden.builder()
                .id(1L)
                .usuario(cliente)
                .estado(EstadoOrden.CANCELADA)
                .total(new BigDecimal("50.00"))
                .metodoPago("Tarjeta de crédito")
                .build();

        when(ordenRepository.findById(1L)).thenReturn(Optional.of(ordenCancelada));

        assertThatThrownBy(() -> ordenService.cancelarOrden(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("La orden ya está cancelada");

        verify(ordenRepository).findById(1L);
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    @DisplayName("Cancelar orden no encontrada - lanza ResourceNotFoundException")
    void cancelarOrden_NotFound_ThrowsResourceNotFoundException() {
        when(ordenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.cancelarOrden(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Orden no encontrada con id: 999");

        verify(ordenRepository).findById(999L);
        verify(ordenRepository, never()).save(any(Orden.class));
    }
}
