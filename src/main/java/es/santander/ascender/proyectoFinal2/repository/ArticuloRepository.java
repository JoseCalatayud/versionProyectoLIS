package es.santander.ascender.proyectoFinal2.repository;

import es.santander.ascender.proyectoFinal2.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    
    Optional<Articulo> findByCodigoBarras(String codigoBarras);
    
    boolean existsByCodigoBarras(String codigoBarras);
    
    List<Articulo> findByBorradoFalse();
    
    List<Articulo> findByFamiliaAndBorradoFalse(String familia);
    
    List<Articulo> findByNombreContainingIgnoreCaseAndBorradoFalse(String nombre);
}