package es.santander.ascender.proyectoFinal2.config;

import es.santander.ascender.proyectoFinal2.model.*;
import es.santander.ascender.proyectoFinal2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("!test") // No ejecutar en perfil de test
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(
            ArticuloRepository articuloRepository,
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository,
            CompraRepository compraRepository) {

        return args -> {
            // Crear usuarios de prueba
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol("ADMIN");
                usuarioRepository.save(admin);

                Usuario user = new Usuario();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRol("USER");
                usuarioRepository.save(user);

                System.out.println("Usuarios creados: admin/admin123 y user/user123");
            }

            // Crear artículos de prueba
            if (articuloRepository.count() == 0) {
                List<Articulo> articulos = new ArrayList<>();

                // Electrónica
                Articulo a1 = new Articulo();
                a1.setNombre("Smartphone XYZ");
                a1.setDescripcion("Teléfono inteligente de última generación");
                a1.setCodigoBarras("8400000001");
                a1.setFamilia("Electrónica");
                a1.setPrecioVenta((599.99));
                a1.setPrecioCompra((450.00));
                a1.setStock(20);
                articulos.add(a1);

                Articulo a2 = new Articulo();
                a2.setNombre("Tablet ABC");
                a2.setDescripcion("Tablet de 10 pulgadas con 128GB");
                a2.setCodigoBarras("8400000002");
                a2.setFamilia("Electrónica");
                a2.setPrecioVenta((399.99));
                a2.setPrecioCompra((300.00));
                a2.setStock(15);
                articulos.add(a2);

                // Alimentación
                Articulo a3 = new Articulo();
                a3.setNombre("Aceite de Oliva Virgen Extra");
                a3.setDescripcion("Aceite de oliva virgen extra 1L");
                a3.setCodigoBarras("8400000003");
                a3.setFamilia("Alimentación");
                a3.setPrecioVenta(7.99);
                a3.setPrecioCompra(5.00);
                a3.setStock(50);
                articulos.add(a3);

                Articulo a4 = new Articulo();
                a4.setNombre("Leche Desnatada");
                a4.setDescripcion("Leche desnatada 1L");
                a4.setCodigoBarras("8400000004");
                a4.setFamilia("Alimentación");
                a4.setPrecioVenta(0.99);
                a4.setPrecioCompra(0.50);
                a4.setStock(100);
                articulos.add(a4);

                // Ropa
                Articulo a5 = new Articulo();
                a5.setNombre("Camiseta Algodón");
                a5.setDescripcion("Camiseta 100% algodón, talla M");
                a5.setCodigoBarras("8400000005");
                a5.setFamilia("Ropa");
                a5.setPrecioVenta(19.99);
                a5.setPrecioCompra(15.00);
                a5.setStock(30);
                articulos.add(a5);

                // Hogar
                Articulo a6 = new Articulo();
                a6.setNombre("Sartén Antiadherente");
                a6.setDescripcion("Sartén antiadherente 24cm");
                a6.setCodigoBarras("8400000006");
                a6.setFamilia("Hogar");
                a6.setPrecioVenta(24.99);
                a6.setPrecioCompra(20.00);
                a6.setStock(25);
                articulos.add(a6);

                // Artículo descatalogado pero con stock
                Articulo a7 = new Articulo();
                a7.setNombre("Teléfono Antiguo");
                a7.setDescripcion("Modelo descatalogado");
                a7.setCodigoBarras("8400000007");
                a7.setFamilia("Electrónica");
                a7.setPrecioVenta(99.99);
                a7.setPrecioCompra(75.00);
                a7.setStock(5);
                a7.setBorrado(true);
                articulos.add(a7);

                articuloRepository.saveAll(articulos);
                System.out.println("Se han creado " + articulos.size() + " artículos de ejemplo");

                // Crear una venta de ejemplo
                Usuario user1 = usuarioRepository.findByUsername("user").orElseThrow();
                Usuario admin1 = usuarioRepository.findByUsername("admin").orElseThrow();

                // Crear una venta
                Venta venta = new Venta();
                venta.setUsuario(user1);
                venta.setFecha(LocalDateTime.now().minusDays(1));

                DetalleVenta detalle1 = new DetalleVenta();
                detalle1.setArticulo(a1);
                detalle1.setCantidad(1);
                detalle1.setPrecioUnitario(a1.getPrecioVenta());
                detalle1.setSubtotal(a1.getPrecioVenta());
                detalle1.setVenta(venta);

                DetalleVenta detalle2 = new DetalleVenta();
                detalle2.setArticulo(a3);
                detalle2.setCantidad(2);
                detalle2.setPrecioUnitario(a3.getPrecioVenta());
                detalle2.setSubtotal(a3.getPrecioVenta()*(2));
                detalle2.setVenta(venta);

                List<DetalleVenta> detalles = new ArrayList<>();
                detalles.add(detalle1);
                detalles.add(detalle2);
                venta.setDetalles(detalles);
                venta.setTotal(detalle1.getSubtotal()+(detalle2.getSubtotal()));

                ventaRepository.save(venta);
                System.out.println("Se ha creado una venta de ejemplo");

                // Crear una compra
                Compra compra = new Compra();
                compra.setUsuario(admin1);
                compra.setFecha(LocalDateTime.now().minusDays(7));

                DetalleCompra detalleCompra1 = new DetalleCompra();
                detalleCompra1.setArticulo(a2);
                detalleCompra1.setCantidad(5);
                detalleCompra1.setPrecioUnitario(300.00);
                detalleCompra1.setSubtotal(1500.00);
                detalleCompra1.setCompra(compra);

                List<DetalleCompra> detallesCompra = new ArrayList<>();
                detallesCompra.add(detalleCompra1);
                compra.setDetalles(detallesCompra);
                compra.setTotal(detalleCompra1.getSubtotal());

                compraRepository.save(compra);
                System.out.println("Se ha creado una compra de ejemplo");
            }
        };
    }
}