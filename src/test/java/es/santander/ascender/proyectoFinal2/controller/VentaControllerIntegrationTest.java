package es.santander.ascender.proyectoFinal2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.santander.ascender.proyectoFinal2.config.TestSecurityConfig;
import es.santander.ascender.proyectoFinal2.model.*;
import es.santander.ascender.proyectoFinal2.repository.ArticuloRepository;
import es.santander.ascender.proyectoFinal2.repository.UsuarioRepository;
import es.santander.ascender.proyectoFinal2.repository.VentaRepository;
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
public class VentaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Articulo articulo;
    private Usuario usuario;

    @BeforeEach
    public void setup() {
        articuloRepository.deleteAll();
        usuarioRepository.deleteAll();
        ventaRepository.deleteAll();

        // Crear usuario para pruebas
        usuario = new Usuario();
        usuario.setUsername("testuser");
        usuario.setPassword(passwordEncoder.encode("password"));
        usuario.setRol("USER");
        usuarioRepository.save(usuario);

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
    public void debeRealizarVentaYActualizarStock() throws Exception {
        // Crear venta con un detalle
        Venta venta = new Venta();
        venta.setUsuario(usuario);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setArticulo(articulo);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(articulo.getPrecioVenta());
        detalle.setSubtotal(articulo.getPrecioVenta()*2);

        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalle);
        venta.setDetalles(detalles);

        // Realizar venta
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(venta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.detalles", hasSize(1)));

        // Verificar que el stock se actualizó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(8))); // 10 original - 2 vendidos = 8
    }

    @Test
    public void debeRechazarVentaSinStockSuficiente() throws Exception {
        // Crear venta con cantidad superior al stock
        Venta venta = new Venta();
        venta.setUsuario(usuario);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setArticulo(articulo);
        detalle.setCantidad(20); // stock es solo 10
        detalle.setPrecioUnitario(articulo.getPrecioVenta());
        detalle.setSubtotal(articulo.getPrecioVenta()*(20));

        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalle);
        venta.setDetalles(detalles);

        // Intentar realizar venta
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(venta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Stock insuficiente")));

        // Verificar que el stock no se modificó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(10))); // stock sin cambios
    }

    @Test
    public void debePermitirAnularVentaYRestaurarStock() throws Exception {
        // Crear venta con un detalle
        Venta venta = new Venta();
        venta.setUsuario(usuario);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setArticulo(articulo);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(articulo.getPrecioVenta());
        detalle.setSubtotal(articulo.getPrecioVenta()* 2);

        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalle);
        venta.setDetalles(detalles);

        // Realizar venta
        String response = mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(venta)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        Venta ventaRealizada = objectMapper.readValue(response, Venta.class);
        Long ventaId = ventaRealizada.getId();

        // Verificar que el stock se actualizó
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(8))); // 10 original - 2 vendidos = 8

        // Anular la venta
        mockMvc.perform(delete("/api/ventas/" + ventaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", containsString("anulada correctamente")));

        // Verificar que el stock se restauró
        mockMvc.perform(get("/api/articulos/" + articulo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(10))); // stock restaurado
    }
}