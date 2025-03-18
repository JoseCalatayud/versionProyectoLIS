package es.santander.ascender.proyectoFinal2.service;

import es.santander.ascender.proyectoFinal2.model.Usuario;
import es.santander.ascender.proyectoFinal2.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorRol(String rol) {
        return usuarioRepository.findByRol(rol);
    }

    @Transactional
    public Usuario crear(Usuario usuario) {
        // Verificar si ya existe un usuario con el mismo username
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + usuario.getUsername());
        }
        
        // Encriptar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuario.getId());
        
        if (usuarioExistente.isEmpty()) {
            throw new IllegalArgumentException("No existe el usuario con ID: " + usuario.getId());
        }
        
        // Si se está cambiando el username, verificar que no exista otro usuario con ese username
        if (!usuarioExistente.get().getUsername().equals(usuario.getUsername()) &&
            usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + usuario.getUsername());
        }
        
        // Si la contraseña ha cambiado, encriptarla
        if (!usuario.getPassword().equals(usuarioExistente.get().getPassword())) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe el usuario con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean esAdmin(Usuario usuario) {
        return usuario != null && "ADMIN".equals(usuario.getRol());
    }
    
    @Transactional(readOnly = true)
    public boolean puedeRealizarVenta(Usuario usuario) {
        // Todos los usuarios pueden realizar ventas
        return usuario != null;
    }
    
    @Transactional(readOnly = true)
    public boolean puedeGestionarArticulos(Usuario usuario) {
        // Solo los administradores pueden gestionar artículos
        return esAdmin(usuario);
    }
    
    @Transactional(readOnly = true)
    public boolean puedeRealizarCompras(Usuario usuario) {
        // Solo los administradores pueden realizar compras
        return esAdmin(usuario);
    }
}