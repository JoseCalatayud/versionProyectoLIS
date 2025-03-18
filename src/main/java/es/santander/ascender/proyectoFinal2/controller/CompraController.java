package es.santander.ascender.proyectoFinal2.controller;

import es.santander.ascender.proyectoFinal2.model.Compra;
import es.santander.ascender.proyectoFinal2.model.Usuario;
import es.santander.ascender.proyectoFinal2.service.CompraService;
import es.santander.ascender.proyectoFinal2.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestionar las operaciones relacionadas con compras.
 * Solo los usuarios con rol ADMIN pueden acceder a estos endpoints.
 */
@RestController
@RequestMapping("/api/compras")
@PreAuthorize("hasRole('ADMIN')")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Obtiene un listado de todas las compras registradas en el sistema.
     * 
     * @return Lista de compras con estado 200 OK
     */
    @GetMapping
    public ResponseEntity<List<Compra>> listarCompras() {
        return ResponseEntity.ok(compraService.listarTodas());
    }

    /**
     * Busca una compra específica según su ID.
     * 
     * @param id Identificador único de la compra
     * @return La compra encontrada con estado 200 OK o 404 Not Found si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Compra> buscarPorId(@PathVariable Long id) {
        Optional<Compra> compra = compraService.buscarPorId(id);
        return compra.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Filtra las compras realizadas en un rango de fechas específico.
     * 
     * @param fechaInicio Fecha inicial del rango de búsqueda (formato ISO)
     * @param fechaFin Fecha final del rango de búsqueda (formato ISO)
     * @return Lista de compras que coinciden con el criterio de fechas
     * Ejemplo de formato de la feche en json "2021-10-10T00:00:00"
     */
    @GetMapping("/fechas")
    public ResponseEntity<List<Compra>> buscarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(compraService.buscarPorFechas(fechaInicio, fechaFin));
    }

    /**
     * Registra una nueva compra de artículos en el sistema.
     * Incrementa el stock de los artículos comprados automáticamente.
     * 
     * @param compra Datos de la compra a realizar con sus detalles
     * @return La compra creada con estado 201 Created o error con 400 Bad Request
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Realizar nueva compra", description = "Crea una nueva compra asociada al usuario autenticado")
    @PostMapping
    public ResponseEntity<?> realizarCompra(@Valid @RequestBody Compra compra) {
        try {
            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<Usuario> usuario = usuarioService.buscarPorUsername(auth.getName());

            if (usuario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Usuario no autenticado"));
            }

            // Verificar que es administrador
            if (!usuarioService.esAdmin(usuario.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", "No tiene permisos para realizar compras"));
            }

            compra.setUsuario(usuario.get());
            Compra nuevaCompra = compraService.realizarCompra(compra);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCompra);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Anula una compra existente y ajusta el stock de los artículos afectados.
     * 
     * @param id Identificador único de la compra a anular
     * @return Mensaje de confirmación con estado 200 OK o error con 400 Bad Request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> anularCompra(@PathVariable Long id) {
        try {
            compraService.anularCompra(id);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Compra anulada correctamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}