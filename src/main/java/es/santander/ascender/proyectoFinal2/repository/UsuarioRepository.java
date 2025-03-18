package es.santander.ascender.proyectoFinal2.repository;

import es.santander.ascender.proyectoFinal2.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<Usuario> findByRol(String rol);
}