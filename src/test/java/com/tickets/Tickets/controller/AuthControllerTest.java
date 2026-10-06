package com.tickets.Tickets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickets.Tickets.domain.Rol;
import com.tickets.Tickets.domain.Usuario;
import com.tickets.Tickets.dto.request.LoginRequest;
import com.tickets.Tickets.dto.request.RegistroRequest;
import com.tickets.Tickets.dto.response.JwtResponse;
import com.tickets.Tickets.dto.response.UsuarioResponse;
import com.tickets.Tickets.repository.UsuarioRepository;
import com.tickets.Tickets.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private LoginRequest loginRequest;
    private RegistroRequest registroRequest;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        Usuario usuario = Usuario.builder()
                .nombre("Juan Pérez")
                .email("juan@example.com")
                .password(passwordEncoder.encode("password123"))
                .rol(Rol.CLIENTE)
                .build();
        usuarioRepository.save(usuario);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@example.com");
        loginRequest.setPassword("password123");

        registroRequest = new RegistroRequest();
        registroRequest.setNombre("María García");
        registroRequest.setEmail("maria@example.com");
        registroRequest.setPassword("password456");
        registroRequest.setRol(Rol.CLIENTE);
    }

    @Test
    @DisplayName("POST /api/auth/login - login exitoso retorna 200 y token JWT")
    void login_Success_Returns200AndJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("juan@example.com"))
                .andExpect(jsonPath("$.usuario.nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("POST /api/auth/login - credenciales inválidas retorna 401")
    void login_InvalidCredentials_Returns401() throws Exception {
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("POST /api/auth/login - email no registrado retorna 401")
    void login_EmailNotRegistered_Returns401() throws Exception {
        loginRequest.setEmail("noexiste@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("POST /api/auth/login - datos inválidos retorna 400")
    void login_InvalidData_Returns400() throws Exception {
        loginRequest.setEmail("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/registro - registro exitoso retorna 201 y token JWT")
    void registro_Success_Returns201AndJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("maria@example.com"))
                .andExpect(jsonPath("$.usuario.nombre").value("María García"));
    }

    @Test
    @DisplayName("POST /api/auth/registro - email ya registrado retorna 400")
    void registro_EmailAlreadyExists_Returns400() throws Exception {
        RegistroRequest registroDuplicado = new RegistroRequest();
        registroDuplicado.setNombre("Juan Pérez");
        registroDuplicado.setEmail("juan@example.com");
        registroDuplicado.setPassword("password123");
        registroDuplicado.setRol(Rol.CLIENTE);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDuplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El email ya está registrado"));
    }

    @Test
    @DisplayName("POST /api/auth/registro - datos inválidos retorna 400")
    void registro_InvalidData_Returns400() throws Exception {
        registroRequest.setNombre("");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/registro - password muy corta retorna 400")
    void registro_ShortPassword_Returns400() throws Exception {
        registroRequest.setPassword("123");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/registro - email inválido retorna 400")
    void registro_InvalidEmail_Returns400() throws Exception {
        registroRequest.setEmail("email-invalido");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroRequest)))
                .andExpect(status().isBadRequest());
    }
}
