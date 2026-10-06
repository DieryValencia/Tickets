package com.tickets.Tickets.bootstrap;

import com.tickets.Tickets.domain.*;
import com.tickets.Tickets.repository.EventoRepository;
import com.tickets.Tickets.repository.TicketRepository;
import com.tickets.Tickets.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataSeeder {

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner seedData() {
        return args -> {
            if (usuarioRepository.count() > 0) {
                log.info("DataSeeder: ya existen datos, omitiendo sembrado.");
                return;
            }

            log.info("DataSeeder: iniciando sembrado de datos...");

            Usuario admin = usuarioRepository.save(Usuario.builder()
                    .nombre("Admin Principal")
                    .email("admin@tickets.com")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMIN)
                    .build());

            Usuario organizador = usuarioRepository.save(Usuario.builder()
                    .nombre("Carlos Organizador")
                    .email("organizador@tickets.com")
                    .password(passwordEncoder.encode("org123"))
                    .rol(Rol.ORGANIZADOR)
                    .build());

            Usuario cliente = usuarioRepository.save(Usuario.builder()
                    .nombre("María Cliente")
                    .email("cliente@tickets.com")
                    .password(passwordEncoder.encode("cliente123"))
                    .rol(Rol.CLIENTE)
                    .build());

            Evento concierto = eventoRepository.save(Evento.builder()
                    .nombre("Concierto Rock Nacional")
                    .descripcion("Gran concierto de rock con bandas locales e internacionales")
                    .fecha(LocalDateTime.now().plusDays(30))
                    .lugar("Estadio Nacional")
                    .capacidad(500)
                    .precioBase(new BigDecimal("50.00"))
                    .categoria("Musica")
                    .imagenUrl("https://example.com/concierto.jpg")
                    .organizador(organizador)
                    .build());

            Evento conferencia = eventoRepository.save(Evento.builder()
                    .nombre("Conferencia Tech 2026")
                    .descripcion("Conferencia de tecnología e innovación")
                    .fecha(LocalDateTime.now().plusDays(60))
                    .lugar("Centro de Convenciones")
                    .capacidad(300)
                    .precioBase(new BigDecimal("80.00"))
                    .categoria("Tecnologia")
                    .imagenUrl("https://example.com/tech.jpg")
                    .organizador(organizador)
                    .build());

            Evento teatro = eventoRepository.save(Evento.builder()
                    .nombre("Obra de Teatro Clásica")
                    .descripcion("Presentación de teatro clásico en vivo")
                    .fecha(LocalDateTime.now().plusDays(15))
                    .lugar("Teatro Municipal")
                    .capacidad(200)
                    .precioBase(new BigDecimal("35.00"))
                    .categoria("Teatro")
                    .imagenUrl("https://example.com/teatro.jpg")
                    .organizador(admin)
                    .build());

            List<Ticket> tickets = new ArrayList<>();
            tickets.addAll(generarTickets(concierto, TipoTicket.GENERAL, 10));
            tickets.addAll(generarTickets(concierto, TipoTicket.VIP, 5));
            tickets.addAll(generarTickets(conferencia, TipoTicket.GENERAL, 8));
            tickets.addAll(generarTickets(conferencia, TipoTicket.PLATINUM, 3));
            tickets.addAll(generarTickets(teatro, TipoTicket.GENERAL, 6));
            tickets.addAll(generarTickets(teatro, TipoTicket.ESTUDIANTE, 4));

            ticketRepository.saveAll(tickets);

            log.info("DataSeeder: sembrado completado. Usuarios: {}, Eventos: {}, Tickets: {}",
                    usuarioRepository.count(), eventoRepository.count(), ticketRepository.count());
        };
    }

    private List<Ticket> generarTickets(Evento evento, TipoTicket tipo, int cantidad) {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            tickets.add(Ticket.builder()
                    .codigo(tipo.name().substring(0, 3) + "-" + evento.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .tipo(tipo)
                    .precio(tipo.calcularPrecio(evento.getPrecioBase()))
                    .estado(EstadoTicket.DISPONIBLE)
                    .evento(evento)
                    .beneficios(tipo.getBeneficios())
                    .build());
        }
        return tickets;
    }
}
