package com.tickets.Tickets.mapper;

import com.tickets.Tickets.domain.Orden;
import com.tickets.Tickets.dto.response.OrdenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, TicketMapper.class})
public interface OrdenMapper {
    OrdenMapper INSTANCE = Mappers.getMapper(OrdenMapper.class);

    OrdenResponse toResponse(Orden orden);
    List<OrdenResponse> toResponseList(List<Orden> ordenes);
}
