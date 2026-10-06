package com.tickets.Tickets.repository;

import com.tickets.Tickets.domain.EstadoTicket;
import com.tickets.Tickets.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventoId(Long eventoId);
    List<Ticket> findByEstado(EstadoTicket estado);
    Optional<Ticket> findByCodigo(String codigo);
}
