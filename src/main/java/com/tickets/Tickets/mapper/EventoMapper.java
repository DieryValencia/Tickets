package com.tickets.Tickets.mapper;

import com.tickets.Tickets.domain.Evento;
import com.tickets.Tickets.dto.response.EventoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EventoMapper {
    EventoMapper INSTANCE = Mappers.getMapper(EventoMapper.class);

    @Mapping(source = "organizador.nombre", target = "organizadorNombre")
    EventoResponse toResponse(Evento evento);
}
