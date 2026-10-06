package com.tickets.Tickets.mapper;

import com.tickets.Tickets.domain.Usuario;
import com.tickets.Tickets.dto.response.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    UsuarioResponse toResponse(Usuario usuario);
}
