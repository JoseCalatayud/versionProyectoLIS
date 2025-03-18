package es.santander.ascender.proyectoFinal2.controller;

import es.santander.ascender.proyectoFinal2.model.Usuario;
import es.santander.ascender.proyectoFinal2.model.Venta;
import es.santander.ascender.proyectoFinal2.service.UsuarioService;
import es.santander.ascender.proyectoFinal2.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Venta>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Venta> venta = ventaService.buscarPorId(id);
        return venta.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<Venta>> buscarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(ventaService.buscarPorFechas(fechaInicio, fechaFin));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLEADO')")
    @Operation(summary = "Realizar nueva venta", description = "Crea una nueva venta asociada al usuario autenticado")
    public ResponseEntity<?> realizarVenta(@RequestBody Venta venta) {
        try {
            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<Usuario> usuario = usuarioService.buscarPorUsername(auth.getName());

            if (usuario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Usuario no autenticado"));
            }

            // Verificar que el usuario tiene permisos para realizar ventas
            if (!usuarioService.puedeRealizarVenta(usuario.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No tiene permisos para realizar ventas"));
            }

            // Asegurar que la venta se asigne al usuario autenticado (ignorando cualquier
            // usuario que venga en la petición)
            venta.setUsuario(usuario.get());

            Venta nuevaVenta = ventaService.realizarVenta(venta);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> anularVenta(@PathVariable Long id) {
        try {
            ventaService.anularVenta(id);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Venta anulada correctamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}