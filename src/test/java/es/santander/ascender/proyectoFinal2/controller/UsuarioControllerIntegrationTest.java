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

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuarioAdmin;
    private Usuario usuarioNormal;

    @BeforeEach
    public void setup() {
        usuarioRepository.deleteAll();

        // Crear usuario admin para pruebas
        usuarioAdmin = new Usuario();
        usuarioAdmin.setUsername("admin");
        usuarioAdmin.setPassword(passwordEncoder.encode("password"));
        usuarioAdmin.setRol("ADMIN");
        usuarioRepository.save(usuarioAdmin);

        // Crear usuario estándar para pruebas
        usuarioNormal = new Usuario();
        usuarioNormal.setUsername("user");
        usuarioNormal.setPassword(passwordEncoder.encode("password"));
        usuarioNormal.setRol("USER");
        usuarioRepository.save(usuarioNormal);
    }

    @Test
    public void debeListarTodosLosUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.username=='admin')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.username=='user')]", hasSize(1)));
    }

    @Test
    public void debeBuscarUsuarioPorId() throws Exception {
        Long adminId = usuarioAdmin.getId();
        
        mockMvc.perform(get("/api/usuarios/" + adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.rol", is("ADMIN")));
    }

    @Test
    public void debeBuscarUsuarioPorUsername() throws Exception {
        mockMvc.perform(get("/api/usuarios/username/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.rol", is("USER")));
    }

    @Test
    public void debeCrearNuevoUsuario() throws Exception {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername("cliente1");
        nuevoUsuario.setPassword("password");
        nuevoUsuario.setRol("USER");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("cliente1")));

        // Verificar que el usuario se guardó en la base de datos
        assertTrue(usuarioRepository.findByUsername("cliente1").isPresent());
    }

    @Test
    public void debeActualizarUsuarioExistente() throws Exception {
        Long userId = usuarioNormal.getId();
        
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(userId);
        usuarioActualizado.setUsername("user_updated");
        usuarioActualizado.setPassword("newpassword");
        usuarioActualizado.setRol("USER");

        mockMvc.perform(put("/api/usuarios/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user_updated")));

        // Verificar que se actualizó en la base de datos
        Optional<Usuario> usuarioDB = usuarioRepository.findById(userId);
        assertTrue(usuarioDB.isPresent());
        assertEquals("user_updated", usuarioDB.get().getUsername());
    }

    @Test
    public void debeEliminarUsuario() throws Exception {
        Long userId = usuarioNormal.getId();

        mockMvc.perform(delete("/api/usuarios/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", containsString("eliminado correctamente")));

        // Verificar que ya no existe en la base de datos
        assertFalse(usuarioRepository.existsById(userId));
    }

    @Test
    public void debeBuscarUsuariosPorRol() throws Exception {
        mockMvc.perform(get("/api/usuarios/rol/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("admin")));

        mockMvc.perform(get("/api/usuarios/rol/USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("user")));
    }

    @Test
    public void debeRechazarCreacionUsuarioDuplicado() throws Exception {
        Usuario usuarioDuplicado = new Usuario();
        usuarioDuplicado.setUsername("admin"); // Ya existe
        usuarioDuplicado.setPassword("otroPassword");
        usuarioDuplicado.setRol("USER");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioDuplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Ya existe un usuario")));
    }
}