package es.santander.ascender.proyectoFinal2.service;

import es.santander.ascender.proyectoFinal2.model.Articulo;
import es.santander.ascender.proyectoFinal2.model.DetalleVenta;
import es.santander.ascender.proyectoFinal2.model.Usuario;
import es.santander.ascender.proyectoFinal2.model.Venta;
import es.santander.ascender.proyectoFinal2.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ArticuloService articuloService;

    @Transactional(readOnly = true)
    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Venta> buscarPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Venta> buscarPorUsuario(Usuario usuario) {
        return ventaRepository.findByUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<Venta> buscarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<Venta> buscarPorUsuarioYFechas(Usuario usuario, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findByUsuarioAndFechaBetween(usuario, fechaInicio, fechaFin);
    }

    public Venta realizarVenta(Venta venta) {
        // Verificar que no haya artículos duplicados

        Set<Long> articulosIds = new HashSet<>();
        List<Long> idsRepetidos = new ArrayList<>();
        Double total = 0.0;
        
        for (DetalleVenta detalle : venta.getDetalles()) {
            Long articuloId = detalle.getArticulo().getId();
            if (!articulosIds.add(articuloId)) {
                // Si add() devuelve false, significa que el ID ya existía en el conjunto
                idsRepetidos.add(articuloId);
            }
        }

        if (!idsRepetidos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se permiten artículos repetidos en la misma venta. IDs repetidos: " + idsRepetidos);
        }

        // Verificar stock para cada línea
        for (DetalleVenta detalle : venta.getDetalles()) {
            Optional<Articulo> articuloOpt = articuloService.buscarPorId(detalle.getArticulo().getId());
            if (articuloOpt.isEmpty()) {
                throw new IllegalArgumentException("No existe el artículo con ID: " + detalle.getArticulo().getId());
            }
            int cantidad = detalle.getCantidad();

            // Verificar si hay stock suficiente
            if (!articuloService.hayStockSuficiente(articuloOpt.get().getId(), cantidad)) {
                throw new IllegalStateException(
                        "Stock insuficiente para el artículo: " + articuloOpt.get().getNombre() + " .");
            }

            // Verificar si el artículo está borrado pero tiene stock

            if (articuloOpt.isPresent() && articuloOpt.get().isBorrado() && articuloOpt.get().getStock() < cantidad) {
                throw new IllegalStateException(
                        "El artículo está descatalogado y no tiene stock suficiente: " + articuloOpt.get().getNombre());
            }

        }

        // Actualizar stock para cada línea
        for (DetalleVenta detalle : venta.getDetalles()) {
            Optional<Articulo> articuloOpt = articuloService.buscarPorId(detalle.getArticulo().getId());
            detalle.setArticulo(articuloOpt.get());
            detalle.setVenta(venta);
            detalle.setPrecioUnitario(articuloOpt.get().getPrecioVenta());            
            articuloService.actualizarStock(detalle.getArticulo().getId(), -detalle.getCantidad());
            total=+detalle.getSubtotal();
        }
        venta.setTotal(total);
        venta.setFecha(LocalDateTime.now());
        // Guardar la venta
        return ventaRepository.save(venta);
    }

    public void anularVenta(Long id) {
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (ventaOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe la venta con ID: " + id);
        }

        Venta venta = ventaOpt.get();
        // Devolver stock al inventario
        for (DetalleVenta detalle : venta.getDetalles()) {
            articuloService.actualizarStock(detalle.getArticulo().getId(), detalle.getCantidad());
        }
        // Eliminar la venta
        ventaRepository.deleteById(id);
    }
}