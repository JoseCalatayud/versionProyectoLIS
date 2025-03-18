package es.santander.ascender.proyectoFinal2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.santander.ascender.proyectoFinal2.config.TestSecurityConfig;
import es.santander.ascender.proyectoFinal2.model.*;
import es.santander.ascender.proyectoFinal2.repository.ArticuloRepository;
import es.santander.ascender.proyectoFinal2.repository.CompraRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class CompraControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Articulo articulo;
    private Usuario usuarioAdmin;

    @BeforeEach
    public void setup() {
        articuloRepository.deleteAll();
        usuarioRepository.deleteAll();
        compraRepository.deleteAll();

        // Crear usuario admin para pruebas
        usuarioAdmin = new Usuario();
        usuarioAdmin.setUsername("admin");
        usuarioAdmin.setPassword(passwordEncoder.encode("password"));
        usuarioAdmin.setRol("ADMIN");
        usuarioRepository.save(usuarioAdmin);

        // Crear artículo para pruebas
        articulo = new Articulo();
        articulo.setNombre("Test Articulo");
        articulo.setCodigoBarras("1234567890123");
        articulo.setFamilia("Electrónica");
        articulo.setPrecioVenta(99.99);
        articulo.setStock(10);
        articuloRepository.save(articulo);
    }

    @Test
    public void debeRealizarCompraYActualizarStock() throws Exception {
        // Crear compra con un detalle
        Compra compra = new Compra();
        compra.setUsuario(usuarioAdmin);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setArticulo(articulo);
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(99.99); // precio de compra
        detalle.setSubtotal(80.00 * 5);

        List<DetalleCompra> detalles = new ArrayList<>();
        detalles.add(detalle);
        compra.setDetalles(detalles);

        // Realizar compra
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compra)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.detalles", hasSize(1)));

        // Verificar que el stock se actualizó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(15))); // 10 original + 5 comprados = 15
    }

    @Test
    public void debeRechazarCompraArticuloBorrado() throws Exception {
        // Marcar artículo como borrado
        articulo.setBorrado(true);
        articuloRepository.save(articulo);

        // Crear compra con un detalle de artículo borrado
        Compra compra = new Compra();
        compra.setUsuario(usuarioAdmin);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setArticulo(articulo);
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(80.00);
        detalle.setSubtotal(80.00*5);

        List<DetalleCompra> detalles = new ArrayList<>();
        detalles.add(detalle);
        compra.setDetalles(detalles);

        // Intentar realizar compra
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compra)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("descatalogado")));

        // Verificar que el stock no se modificó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(10))); // stock sin cambios
    }

    @Test
    public void debePermitirAnularCompraYAjustarStock() throws Exception {
        // Crear compra con un detalle
        Compra compra = new Compra();
        compra.setUsuario(usuarioAdmin);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setArticulo(articulo);
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(80.00);
        detalle.setSubtotal(80.00*5);

        List<DetalleCompra> detalles = new ArrayList<>();
        detalles.add(detalle);
        compra.setDetalles(detalles);

        // Realizar compra
        String response = mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compra)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        Compra compraRealizada = objectMapper.readValue(response, Compra.class);
        Long compraId = compraRealizada.getId();

        // Verificar que el stock se actualizó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(15))); // 10 original + 5 comprados = 15

        // Anular la compra
        mockMvc.perform(delete("/api/compras/" + compraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", containsString("anulada correctamente")));

        // Verificar que el stock se ajustó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(10))); // stock restaurado
    }
}