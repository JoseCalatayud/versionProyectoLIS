package es.santander.ascender.proyectoFinal2.service;

import es.santander.ascender.proyectoFinal2.model.Articulo;
import es.santander.ascender.proyectoFinal2.model.Compra;
import es.santander.ascender.proyectoFinal2.model.DetalleCompra;
import es.santander.ascender.proyectoFinal2.model.Usuario;
import es.santander.ascender.proyectoFinal2.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ArticuloService articuloService;

    @Transactional(readOnly = true)
    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Compra> buscarPorId(Long id) {
        return compraRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Compra> buscarPorUsuario(Usuario usuario) {
        return compraRepository.findByUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<Compra> buscarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return compraRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<Compra> buscarPorUsuarioYFechas(Usuario usuario, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return compraRepository.findByUsuarioAndFechaBetween(usuario, fechaInicio, fechaFin);
    }

    public Compra realizarCompra(Compra compra) {
        // Verificar que los artículos existen y no están borrados
        for (DetalleCompra detalle : compra.getDetalles()) {
            Optional<Articulo> articuloOpt = articuloService.buscarPorId(detalle.getArticulo().getId());
            if (articuloOpt.isEmpty()) {
                throw new IllegalArgumentException("No existe el artículo con ID: " + articuloOpt.get().getId());
            }

            // Verificar que el artículo no está borrado
            if (articuloOpt.get().isBorrado()) {
                throw new IllegalStateException(
                        "No se puede comprar el artículo porque está descatalogado: " + articuloOpt.get().getNombre());
            }
            detalle.setArticulo(articuloOpt.get());
            detalle.setCompra(compra);
            detalle.setPrecioUnitario(articuloOpt.get().getPrecioCompra());
            detalle.setSubtotal(detalle.getCantidad() * detalle.getPrecioUnitario());
            articuloService.actualizarStock(detalle.getArticulo().getId(), detalle.getCantidad());
        }
        compra.setFecha(LocalDateTime.now());        
        return compraRepository.save(compra);
    }

    public void anularCompra(Long id) {
        Optional<Compra> compraOpt = compraRepository.findById(id);
        if (compraOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe la compra con ID: " + id);
        }

        Compra compra = compraOpt.get();
        // Restar el stock que se había añadido
        for (DetalleCompra detalle : compra.getDetalles()) {
            articuloService.actualizarStock(detalle.getArticulo().getId(), -detalle.getCantidad());
        }

        // Eliminar la compra
        compraRepository.deleteById(id);
    }
}