package org.example.model;

import jakarta.persistence.*;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Producto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// marca la clase como una entidad JPA (se persistirá en la base de datos)
@Entity
// nombre de la tabla en la base de datos
@Table(name = "usuario")
public class Usuario {
    // identificador único de la entidad
    @Id
    // el valor del ID se genera automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // campo obligatorio
    @Column(nullable = false)
    private String nombre;

    // campos opcionales
    private String direccion;
    private String telefono;

    // campo obligatorio
    @Column(nullable = false)
    private LocalDate fechaNacimiento;
    // campo obligatorio y único
    @Column(nullable = false, unique = true)
    private String dni;
    // campo obligatorio y único
    @Column(nullable = false, unique = true)
    private String email;
    // campo obligatorio
    @Column(nullable = false)
    private String password;

    // un set de Productos favoritos, para que no se repitan productos favoritos, pero puede tener muchos
    // eager: cuando cargues un usuario en JPA automáticamente carga también todos los productos favoritos
    @ManyToMany(fetch = FetchType.EAGER)
    // se crea tabla intermedia usuario_producto_favorito
    @JoinTable(
            // nombre de la tabla en la base de datos
            name = "usuario_producto_favorito",
            // define la columna de la FK que apunta al usuario en la tabla intermedia
            joinColumns = @JoinColumn(name = "usuario_id"),
            // define la columna de la FK que apunta al producto en la tabla intermedia
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private Set<Producto> favoritos = new HashSet<>();

    // un set de pedidos (para que no se repitan los mismos pedidos) ya que un usuario puede tener muchos pedidos, lo hacemos Linkedhash para que se ordene por inserción
    // mappedBy indica que la FK está en Pedido (campo usuario)
    // all: persistencia propagada a pedidos
    // lazy: el producto no se carga hasta que se accede explícitamente
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Pedido> pedidos = new LinkedHashSet<>();

    // constructor
    // no id
    public Usuario(String nombre, String dni, String direccion, LocalDate fechaNacimiento,
                   String telefono, String email, String password) {
        this.nombre = nombre;
        this.dni = dni;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        this.password = password;
        //no poner nada en favoritos para empezar y tampoco tiene pedidos
    }

    public Usuario(long id) {
        this.id = id;
    }

    // constructor vacío obligatorio para JPA
    public Usuario() {
    }


    //getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Producto> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(Set<Producto> favoritos) {
        this.favoritos = favoritos;
    }

    public Set<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(Set<Pedido> pedidos) {
        this.pedidos = pedidos;
    }


    // hasCode y equals por los Set
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", dni='" + dni + '\'' +
                ", direccion='" + direccion + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", favoritos=" + favoritos +
                '}';
    }
}
