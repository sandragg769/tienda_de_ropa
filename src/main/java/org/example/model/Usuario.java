package org.example.model;

import org.example.model.pedido.Pedido;
import org.example.model.producto.Producto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Usuario {
    private long id;
    private String dni;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String passsword;
    //un set de Productos favoritos, para que no se repitan productos favoritos, pero puede tener muchos
    private Set<Producto> favoritos = new HashSet<>();
    // un set de pedidos (para que no se repitan los mismos pedidos) ya que un usuario puede tener muchos pedidos, lo hacemos Linkedhash para que se ordene por insercción
    private Set<Pedido> pedidos = new LinkedHashSet<>();

    //constructor
    public Usuario(long id, String dni, String direccion, LocalDate fechaNacimiento,
                   String telefono, String email, String passsword) {
        this.id = id;
        this.dni = dni;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        this.passsword = passsword;
        //no poner nada en favoritos para empezar y tampoco tiene pedidos
    }

    //getters y setters
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

    public String getPasssword() {
        return passsword;
    }

    public void setPasssword(String passsword) {
        this.passsword = passsword;
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

    //hascode y equals por los Set
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
}
