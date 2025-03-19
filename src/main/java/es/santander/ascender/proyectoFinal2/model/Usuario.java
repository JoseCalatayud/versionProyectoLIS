package es.santander.ascender.proyectoFinal2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "username"))

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Venta> ventas = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Compra> compras = new ArrayList<>();

    // Constructor vacío
    public Usuario() {
    }
    // Constructor con parámetros
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
        
    }

    // Constructor con parámetros
    public Usuario(String username, String password, RolUsuario rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    // Método para compatibilidad con código existente que espera un String
    public void setRol(String rol) {
        try {
            this.rol = RolUsuario.valueOf(rol);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + rol + ". Los valores permitidos son: ADMIN, USER");
        }
    }

    public List<Venta> getVentas() {
        return ventas;
    }

    public void setVentas(List<Venta> ventas) {
        this.ventas = ventas;
    }

    public List<Compra> getCompras() {
        return compras;
    }

    public void setCompras(List<Compra> compras) {
        this.compras = compras;
    }

    // Métodos de utilidad
    public boolean isAdmin() {
        return RolUsuario.ADMIN.equals(this.rol);
    }
    
    public boolean puedeRealizarVenta() {
        return true; // Todos los usuarios pueden hacer ventas
    }
    
    public boolean puedeGestionarArticulos() {
        return isAdmin(); // Solo admins pueden gestionar artículos
    }
    
    public boolean puedeRealizarCompras() {
        return isAdmin(); // Solo admins pueden realizar compras
    }
}