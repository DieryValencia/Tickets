package com.tickets.Tickets.service;

import com.tickets.Tickets.domain.Rol;
import com.tickets.Tickets.domain.Usuario;
import com.tickets.Tickets.dto.request.LoginRequest;
import com.tickets.Tickets.dto.request.RegistroRequest;
import com.tickets.Tickets.dto.response.JwtResponse;
import com.tickets.Tickets.dto.response.UsuarioResponse;
import com.tickets.Tickets.exception.BadRequestException;
import com.tickets.Tickets.exception.UnauthorizedException;
import com.tickets.Tickets.mapper.UsuarioMapper;
import com.tickets.Tickets.repository.UsuarioRepository;
import com.tickets.Tickets.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name());

        return JwtResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuario(UsuarioMapper.INSTANCE.toResponse(usuario))
                .build();
    }

    @Transactional
    public JwtResponse registro(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Rol rol = request.getRol() != null ? request.getRol() : Rol.CLIENTE;

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name());

        return JwtResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuario(UsuarioMapper.INSTANCE.toResponse(usuario))
                .build();
    }
}
