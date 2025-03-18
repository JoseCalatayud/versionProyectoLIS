package es.santander.ascender.proyectoFinal2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.santander.ascender.proyectoFinal2.config.TestSecurityConfig;
import es.santander.ascender.proyectoFinal2.model.Usuario;
import es.santander.ascender.proyectoFinal2.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        usuarioRepository.deleteAll();

        // Crear usuario admin para pruebas
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);

        // Crear usuario estándar para pruebas
        Usuario user = new Usuario();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRol("USER");
        usuarioRepository.save(user);
    }

    @Test
    public void debePermitirLoginAdminCorrecto() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Inicio de sesión exitoso")));
    }

    @Test
    public void debePermitirLoginUsuarioEstandarCorrecto() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "user");
        credentials.put("password", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", is("Inicio de sesión exitoso")));
    }

    @Test
    public void debeRechazarLoginIncorrecto() throws Exception {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin");
        credentials.put("password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void debePermitirRegistroDeNuevoUsuario() throws Exception {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername("nuevousuario");
        nuevoUsuario.setPassword("password");
        nuevoUsuario.setRol("USER");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("nuevousuario")));
                
        // Verificar que el usuario se ha creado en la BD
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", "nuevousuario", "password", "password"))))
                .andExpect(status().isOk());
    }

    @Test
    public void debeRechazarRegistroUsuarioDuplicado() throws Exception {
        Usuario usuarioDuplicado = new Usuario();
        usuarioDuplicado.setUsername("admin");
        usuarioDuplicado.setPassword("otropassword");
        usuarioDuplicado.setRol("USER");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioDuplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Ya existe un usuario")));
    }
}