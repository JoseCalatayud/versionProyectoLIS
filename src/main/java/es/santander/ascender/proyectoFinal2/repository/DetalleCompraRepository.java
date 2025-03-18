package es.santander.ascender.proyectoFinal2.repository;

import es.santander.ascender.proyectoFinal2.model.Articulo;
import es.santander.ascender.proyectoFinal2.model.Compra;
import es.santander.ascender.proyectoFinal2.model.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {
    
    List<DetalleCompra> findByCompra(Compra compra);
    
    List<DetalleCompra> findByArticulo(Articulo articulo);
}