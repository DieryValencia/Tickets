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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<EventoResponse> listarTodos() {
        return eventoRepository.findAll().stream()
                .map(EventoMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarPorCategoria(String categoria) {
        return eventoRepository.findByCategoria(categoria).stream()
                .map(EventoMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarProximos() {
        return eventoRepository.findByFechaAfter(LocalDateTime.now()).stream()
                .map(EventoMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse obtenerPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        return EventoMapper.INSTANCE.toResponse(evento);
    }

    @Transactional
    public EventoResponse crear(EventoRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario organizador = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Organizador no encontrado"));

        if (organizador.getRol() != Rol.ADMIN && organizador.getRol() != Rol.ORGANIZADOR) {
            throw new UnauthorizedException("Solo ADMIN u ORGANIZADOR pueden crear eventos");
        }

        Evento evento = Evento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .fecha(request.getFecha())
                .lugar(request.getLugar())
                .capacidad(request.getCapacidad())
                .precioBase(request.getPrecioBase())
                .categoria(request.getCategoria())
                .imagenUrl(request.getImagenUrl())
                .organizador(organizador)
                .build();

        return EventoMapper.INSTANCE.toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponse actualizar(Long id, EventoRequest request) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));

        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setFecha(request.getFecha());
        evento.setLugar(request.getLugar());
        evento.setCapacidad(request.getCapacidad());
        evento.setPrecioBase(request.getPrecioBase());
        evento.setCategoria(request.getCategoria());
        evento.setImagenUrl(request.getImagenUrl());

        return EventoMapper.INSTANCE.toResponse(eventoRepository.save(evento));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!eventoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Evento no encontrado con id: " + id);
        }
        eventoRepository.deleteById(id);
    }
}
