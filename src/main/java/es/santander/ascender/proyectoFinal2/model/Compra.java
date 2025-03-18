package es.santander.ascender.proyectoFinal2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Double total;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalleCompra> detalles = new ArrayList<>();

    // Constructor vacío
    public Compra() {
        this.fecha = LocalDateTime.now();
        this.total = 0.0;
    }

    // Constructor con parámetros
    public Compra(Usuario usuario) {
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.total = 0.0;
    }

    // Método para agregar un detalle
    public void agregarDetalle(DetalleCompra detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
        // Actualizar el total
        this.total = this.total+(detalle.getSubtotal());
    }

    // Método para eliminar un detalle
    public void eliminarDetalle(DetalleCompra detalle) {
        detalles.remove(detalle);
        detalle.setCompra(null);
        // Actualizar el total
        this.total = this.total-(detalle.getSubtotal());
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
}