package com.tickets.Tickets.mapper;

import com.tickets.Tickets.domain.Ticket;
import com.tickets.Tickets.dto.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);

    @Mapping(source = "evento.nombre", target = "eventoNombre")
    TicketResponse toResponse(Ticket ticket);
}
