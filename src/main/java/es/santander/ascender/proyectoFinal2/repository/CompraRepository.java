package es.santander.ascender.proyectoFinal2.repository;

import es.santander.ascender.proyectoFinal2.model.Compra;
import es.santander.ascender.proyectoFinal2.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    
    List<Compra> findByUsuario(Usuario usuario);
    
    List<Compra> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<Compra> findByUsuarioAndFechaBetween(Usuario usuario, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}