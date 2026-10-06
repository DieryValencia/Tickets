package com.tickets.Tickets.service;

import com.tickets.Tickets.domain.Rol;
import com.tickets.Tickets.domain.Usuario;
import com.tickets.Tickets.dto.request.LoginRequest;
import com.tickets.Tickets.dto.request.RegistroRequest;
import com.tickets.Tickets.dto.response.JwtResponse;
import com.tickets.Tickets.exception.BadRequestException;
import com.tickets.Tickets.exception.UnauthorizedException;
import com.tickets.Tickets.mapper.UsuarioMapper;
import com.tickets.Tickets.repository.UsuarioRepository;
import com.tickets.Tickets.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private RegistroRequest registroRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@example.com")
                .password("encodedPassword")
                .rol(Rol.CLIENTE)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@example.com");
        loginRequest.setPassword("password123");

        registroRequest = new RegistroRequest();
        registroRequest.setNombre("Juan Pérez");
        registroRequest.setEmail("juan@example.com");
        registroRequest.setPassword("password123");
        registroRequest.setRol(Rol.CLIENTE);
    }

    @Test
    @DisplayName("Login exitoso - retorna token JWT")
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("juan@example.com", "CLIENTE")).thenReturn("jwt-token");

        JwtResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getUsuario()).isNotNull();
        assertThat(response.getUsuario().getEmail()).isEqualTo("juan@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail("juan@example.com");
        verify(jwtService).generateToken("juan@example.com", "CLIENTE");
    }

    @Test
    @DisplayName("Login con credenciales inválidas - lanza UnauthorizedException")
    void login_InvalidCredentials_ThrowsUnauthorizedException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login con usuario no encontrado - lanza UnauthorizedException")
    void login_UserNotFound_ThrowsUnauthorizedException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail("juan@example.com");
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Registro exitoso - crea usuario y retorna token JWT")
    void registro_Success() {
        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken("juan@example.com", "CLIENTE")).thenReturn("jwt-token");

        JwtResponse response = authService.registro(registroRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getUsuario()).isNotNull();
        assertThat(response.getUsuario().getEmail()).isEqualTo("juan@example.com");
        verify(usuarioRepository).existsByEmail("juan@example.com");
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(jwtService).generateToken("juan@example.com", "CLIENTE");
    }

    @Test
    @DisplayName("Registro con email ya registrado - lanza BadRequestException")
    void registro_EmailAlreadyExists_ThrowsBadRequestException() {
        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registro(registroRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("El email ya está registrado");

        verify(usuarioRepository).existsByEmail("juan@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Registro con rol nulo - asigna rol CLIENTE por defecto")
    void registro_NullRole_AssignsDefaultRole() {
        registroRequest.setRol(null);
        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken("juan@example.com", "CLIENTE")).thenReturn("jwt-token");

        JwtResponse response = authService.registro(registroRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro con rol ADMIN - asigna rol ADMIN")
    void registro_AdminRole_AssignsAdminRole() {
        registroRequest.setRol(Rol.ADMIN);
        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken("juan@example.com", "ADMIN")).thenReturn("jwt-token");

        JwtResponse response = authService.registro(registroRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(usuarioRepository).save(any(Usuario.class));
    }
}
