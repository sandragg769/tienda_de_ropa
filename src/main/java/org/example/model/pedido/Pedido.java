package org.example.model.pedido;

import jakarta.persistence.*;
import org.example.model.Usuario;

import java.time.LocalDate;
import java.util.*;


public class Pedido {
    //no hace falta transient por la referencia circular
    private long id;
    private LocalDate fecha;
    private EstadoPedido estado;
    //creo un objeto y no una lista porque un pedido es tenido por un usuario solo
    private transient Usuario usuario;
    //uso una lista para poder tener muchas lineasPedido en un producto (pueden repetirse)
    private Set<LineaPedido> lineasPedido = new HashSet<>();

    //constructor
    //no id
    public Pedido(Usuario usuario) {
        this.usuario = usuario;
        //poner fecha del día que se hace el pedido, con el Date se guarda automáticamente
        this.fecha = LocalDate.now();
        //el pedido está pendiente ya que se ha creado
        this.estado = EstadoPedido.PENDIENTE;
    }

    public Pedido() {
    }

    //getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<LineaPedido> getLineasPedido() {
        return lineasPedido;
    }

    public void setLineasPedido(Set<LineaPedido> lineasPedido) {
        this.lineasPedido = lineasPedido;
    }

    //Este metodo nos devuelve la suma de las líneas del pedido
    //cogemos la lista de líneas de pedido, obtenemos los subtotales de cada uno y los sumamos
    public double getPecioTotal() {
        return lineasPedido.stream()
                //double ya que hay que devolver double
                .mapToDouble(LineaPedido::getPrecioSubTotal)
                //sumar
                .sum();
    }

    //métodos de cambiar estado (corrección profesor) NO SE CAMBIA EN EL CONTROLADOR, EL CONTROLADOR SOLO DIRIGE
    public void finalizar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede finalizar un pedido pendiente.");
        }
        estado = EstadoPedido.FINALIZADO;
    }

    public void cancelar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede cancelar un pedido pendiente.");
        }
        estado = EstadoPedido.CANCELADO;
    }

    public void entregar() {
        if (estado != EstadoPedido.FINALIZADO) {
            throw new IllegalStateException("Solo se puede entregar pedidos finalizados.");
        }
        estado = EstadoPedido.ENTREGADO;
    }

    //poner hasCode y equals porque en otras clases tengo Set de pedido
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
