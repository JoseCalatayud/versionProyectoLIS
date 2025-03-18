package es.santander.ascender.proyectoFinal2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.santander.ascender.proyectoFinal2.config.TestSecurityConfig;
import es.santander.ascender.proyectoFinal2.model.Articulo;
import es.santander.ascender.proyectoFinal2.repository.ArticuloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class ArticuloControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        articuloRepository.deleteAll();
    }

    @Test
    public void debeCrearYRecuperarArticulo() throws Exception {
        // Crear un artículo para la prueba
        Articulo articulo = new Articulo();
        articulo.setNombre("Test Articulo");
        articulo.setCodigoBarras("1234567890123");
        articulo.setFamilia("Electrónica");
        articulo.setPrecioVenta(99.99);
        articulo.setStock(10);

        // Crear artículo
        mockMvc.perform(post("/api/articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articulo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Test Articulo")))
                .andExpect(jsonPath("$.codigoBarras", is("1234567890123")))
                .andExpect(jsonPath("$.stock", is(10)));

        // Listar artículos
        mockMvc.perform(get("/api/articulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Test Articulo")));

        // Buscar artículo por código de barras
        mockMvc.perform(get("/api/articulos/codigo/1234567890123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Test Articulo")));
    }

    @Test
    public void debeRechazarArticuloConCodigoBarrasDuplicado() throws Exception {
        // Crear un artículo para la prueba
        Articulo articulo1 = new Articulo();
        articulo1.setNombre("Test Articulo 1");
        articulo1.setCodigoBarras("1234567890123");
        articulo1.setFamilia("Electrónica");
        articulo1.setPrecioVenta(99.99);
        articulo1.setStock(10);

        // Crear primer artículo
        mockMvc.perform(post("/api/articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articulo1)))
                .andExpect(status().isCreated());

        // Intentar crear artículo con mismo código de barras
        Articulo articulo2 = new Articulo();
        articulo2.setNombre("Test Articulo 2");
        articulo2.setCodigoBarras("1234567890123");
        articulo2.setFamilia("Ropa");
        articulo2.setPrecioVenta(99.99);
        articulo2.setStock(20);

        mockMvc.perform(post("/api/articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articulo2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("código de barras")));
    }

    @Test
    public void debeActualizarArticulo() throws Exception {
        // Crear un artículo para la prueba
        Articulo articulo = new Articulo();
        articulo.setNombre("Test Articulo");
        articulo.setCodigoBarras("1234567890123");
        articulo.setFamilia("Electrónica");
        articulo.setPrecioVenta(99.99);
        articulo.setStock(10);

        // Crear artículo
        String response = mockMvc.perform(post("/api/articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articulo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        Articulo articuloCreado = objectMapper.readValue(response, Articulo.class);
        Long id = articuloCreado.getId();

        // Actualizar artículo
        articuloCreado.setNombre("Test Articulo Actualizado");
        articuloCreado.setPrecioVenta(99.99);

        mockMvc.perform(put("/api/articulos/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articuloCreado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Test Articulo Actualizado")))
                .andExpect(jsonPath("$.precioVenta", is(129.99)));
    }

    @Test
    public void debeBorrarLogicamente() throws Exception {
        // Crear un artículo para la prueba
        Articulo articulo = new Articulo();
        articulo.setNombre("Test Articulo");
        articulo.setCodigoBarras("1234567890123");
        articulo.setFamilia("Electrónica");
        articulo.setPrecioVenta(99.99);
        articulo.setStock(10);

        // Crear artículo
        String response = mockMvc.perform(post("/api/articulos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articulo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        Articulo articuloCreado = objectMapper.readValue(response, Articulo.class);
        Long id = articuloCreado.getId();

        // Borrar artículo
        mockMvc.perform(delete("/api/articulos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", containsString("eliminado correctamente")));

        // Verificar que el artículo ya no aparece en el listado (borrado lógico)
        mockMvc.perform(get("/api/articulos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}