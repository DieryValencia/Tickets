package com.tickets.Tickets.service;

import com.tickets.Tickets.domain.Evento;
import com.tickets.Tickets.domain.Rol;
import com.tickets.Tickets.domain.Usuario;
import com.tickets.Tickets.dto.request.EventoRequest;
import com.tickets.Tickets.dto.response.EventoResponse;
import com.tickets.Tickets.exception.BadRequestException;
import com.tickets.Tickets.exception.ResourceNotFoundException;
import com.tickets.Tickets.exception.UnauthorizedException;
import com.tickets.Tickets.mapper.EventoMapper;
import com.tickets.Tickets.repository.EventoRepository;
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
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EventoService eventoService;

    private Evento evento;
    private EventoRequest eventoRequest;
    private Usuario organizador;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        organizador = Usuario.builder()
                .id(1L)
                .nombre("Carlos Organizador")
                .email("organizador@example.com")
                .password("encodedPassword")
                .rol(Rol.ORGANIZADOR)
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
                .imagenUrl("https://example.com/imagen.jpg")
                .organizador(organizador)
                .build();

        eventoRequest = new EventoRequest();
        eventoRequest.setNombre("Concierto Rock");
        eventoRequest.setDescripcion("Gran concierto de rock");
        eventoRequest.setFecha(LocalDateTime.now().plusDays(30));
        eventoRequest.setLugar("Estadio Nacional");
        eventoRequest.setCapacidad(500);
        eventoRequest.setPrecioBase(new BigDecimal("50.00"));
        eventoRequest.setCategoria("Musica");
        eventoRequest.setImagenUrl("https://example.com/imagen.jpg");
    }

    private void setAuthentication(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Listar todos los eventos - retorna lista de eventos")
    void listarTodos_Success() {
        Evento evento2 = Evento.builder()
                .id(2L)
                .nombre("Conferencia Tech")
                .descripcion("Conferencia de tecnología")
                .fecha(LocalDateTime.now().plusDays(60))
                .lugar("Centro de Convenciones")
                .capacidad(300)
                .precioBase(new BigDecimal("80.00"))
                .categoria("Tecnologia")
                .organizador(organizador)
                .build();

        when(eventoRepository.findAll()).thenReturn(List.of(evento, evento2));

        List<EventoResponse> response = eventoService.listarTodos();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getNombre()).isEqualTo("Concierto Rock");
        assertThat(response.get(1).getNombre()).isEqualTo("Conferencia Tech");
        verify(eventoRepository).findAll();
    }

    @Test
    @DisplayName("Listar eventos por categoría - retorna eventos filtrados")
    void listarPorCategoria_Success() {
        when(eventoRepository.findByCategoria("Musica")).thenReturn(List.of(evento));

        List<EventoResponse> response = eventoService.listarPorCategoria("Musica");

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCategoria()).isEqualTo("Musica");
        verify(eventoRepository).findByCategoria("Musica");
    }

    @Test
    @DisplayName("Listar próximos eventos - retorna eventos con fecha futura")
    void listarProximos_Success() {
        when(eventoRepository.findByFechaAfter(any(LocalDateTime.class))).thenReturn(List.of(evento));

        List<EventoResponse> response = eventoService.listarProximos();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        verify(eventoRepository).findByFechaAfter(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Obtener evento por ID - retorna evento encontrado")
    void obtenerPorId_Success() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));

        EventoResponse response = eventoService.obtenerPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Concierto Rock");
        assertThat(response.getLugar()).isEqualTo("Estadio Nacional");
        verify(eventoRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener evento por ID no encontrado - lanza ResourceNotFoundException")
    void obtenerPorId_NotFound_ThrowsResourceNotFoundException() {
        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Evento no encontrado con id: 999");

        verify(eventoRepository).findById(999L);
    }

    @Test
    @DisplayName("Crear evento como ORGANIZADOR - exitoso")
    void crear_AsOrganizador_Success() {
        setAuthentication("organizador@example.com");
        when(usuarioRepository.findByEmail("organizador@example.com")).thenReturn(Optional.of(organizador));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        EventoResponse response = eventoService.crear(eventoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Concierto Rock");
        assertThat(response.getLugar()).isEqualTo("Estadio Nacional");
        verify(usuarioRepository).findByEmail("organizador@example.com");
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    @DisplayName("Crear evento como ADMIN - exitoso")
    void crear_AsAdmin_Success() {
        setAuthentication("admin@example.com");
        when(usuarioRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        EventoResponse response = eventoService.crear(eventoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Concierto Rock");
        verify(usuarioRepository).findByEmail("admin@example.com");
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    @DisplayName("Crear evento como CLIENTE - lanza UnauthorizedException")
    void crear_AsCliente_ThrowsUnauthorizedException() {
        Usuario cliente = Usuario.builder()
                .id(3L)
                .nombre("María Cliente")
                .email("cliente@example.com")
                .password("encodedPassword")
                .rol(Rol.CLIENTE)
                .build();

        setAuthentication("cliente@example.com");
        when(usuarioRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> eventoService.crear(eventoRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Solo ADMIN u ORGANIZADOR pueden crear eventos");

        verify(usuarioRepository).findByEmail("cliente@example.com");
        verify(eventoRepository, never()).save(any(Evento.class));
    }

    @Test
    @DisplayName("Actualizar evento - exitoso")
    void actualizar_Success() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        EventoResponse response = eventoService.actualizar(1L, eventoRequest);

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Concierto Rock");
        verify(eventoRepository).findById(1L);
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    @DisplayName("Actualizar evento no encontrado - lanza ResourceNotFoundException")
    void actualizar_NotFound_ThrowsResourceNotFoundException() {
        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.actualizar(999L, eventoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Evento no encontrado con id: 999");

        verify(eventoRepository).findById(999L);
        verify(eventoRepository, never()).save(any(Evento.class));
    }

    @Test
    @DisplayName("Eliminar evento - exitoso")
    void eliminar_Success() {
        when(eventoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(eventoRepository).deleteById(1L);

        eventoService.eliminar(1L);

        verify(eventoRepository).existsById(1L);
        verify(eventoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar evento no encontrado - lanza ResourceNotFoundException")
    void eliminar_NotFound_ThrowsResourceNotFoundException() {
        when(eventoRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> eventoService.eliminar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Evento no encontrado con id: 999");

        verify(eventoRepository).existsById(999L);
        verify(eventoRepository, never()).deleteById(anyLong());
    }
}
