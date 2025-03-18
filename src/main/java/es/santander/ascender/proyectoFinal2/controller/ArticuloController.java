package es.santander.ascender.proyectoFinal2.controller;

import es.santander.ascender.proyectoFinal2.model.Articulo;
import es.santander.ascender.proyectoFinal2.service.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/articulos")
public class ArticuloController {

    @Autowired
    private ArticuloService articuloService;

    @GetMapping
    public ResponseEntity<List<Articulo>> listarArticulos() {
        return ResponseEntity.ok(articuloService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Articulo> articulo = articuloService.buscarPorId(id);
        return articulo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigoBarras}")
    public ResponseEntity<?> buscarPorCodigoBarras(@PathVariable String codigoBarras) {
        Optional<Articulo> articulo = articuloService.buscarPorCodigoBarras(codigoBarras);
        return articulo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/familia/{familia}")
    public ResponseEntity<List<Articulo>> buscarPorFamilia(@PathVariable String familia) {
        return ResponseEntity.ok(articuloService.buscarPorFamilia(familia));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Articulo>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(articuloService.buscarPorNombre(nombre));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearArticulo(@Valid @RequestBody Articulo articulo) {
        try {
            Articulo nuevoArticulo = articuloService.crear(articulo);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoArticulo);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarArticulo(@PathVariable Long id, @Valid @RequestBody Articulo articulo) {
        try {
            articulo.setId(id); // Asegurar que el ID coincida con el de la ruta
            Articulo actualizadoArticulo = articuloService.actualizar(articulo);
            return ResponseEntity.ok(actualizadoArticulo);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> borrarArticulo(@PathVariable Long id) {
        try {
            articuloService.borradoLogico(id);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Artículo eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verificar/{codigoBarras}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> verificarExistencia(@PathVariable String codigoBarras) {
        boolean existe = articuloService.existeArticulo(codigoBarras);
        Map<String, Object> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }
}