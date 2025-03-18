package es.santander.ascender.proyectoFinal2.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "detalles_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonBackReference
    private Compra compra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double subtotal;

    // Constructor vacío
    public DetalleCompra() {
    }

    // Constructor con parámetros
    public DetalleCompra(Articulo articulo, Integer cantidad, Double precioUnitario) {
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioUnitario = articulo.getPrecioCompra();
        this.subtotal = precioUnitario * cantidad;
    }
    
    // Constructor con todos los parámetros
    public DetalleCompra(Long id, Compra compra, Articulo articulo, Integer cantidad, Double precioUnitario) {
        this.id = id;
        this.compra = compra;
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioUnitario = articulo.getPrecioCompra();
        this.subtotal = cantidad * precioUnitario;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        // Recalcular el subtotal
        if (this.precioUnitario != null) {
            this.subtotal = this.precioUnitario * cantidad ;
        }
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
        // Recalcular el subtotal
        if (this.cantidad != null) {
            this.subtotal = precioUnitario*cantidad;
        }
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}