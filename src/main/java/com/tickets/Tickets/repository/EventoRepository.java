package com.tickets.Tickets.repository;

import com.tickets.Tickets.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByCategoria(String categoria);
    List<Evento> findByFechaAfter(LocalDateTime fecha);
}
