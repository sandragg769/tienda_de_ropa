package org.example.model.pedido;

import org.example.model.Usuario;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Pedido {
    private long id;
    private Date fecha;
    private EstadoPedido estado;
    //creo un objeto y no una lista porque un pedido es tenido por un usuario solo
    private Usuario usuario;
    //uso una lista para poder tener muchas lineasPedido en un producto (pueden repetirse)
    private List<LineaPedido> lineasPedido = new ArrayList<>();

    //constructor
    public Pedido(long id, Usuario usuario) {
        this.id = id;
        this.usuario = usuario;
        //poner fecha del día que se hace el pedido, con el Date se guarda automáticamente
        this.fecha = new Date();
        //el pedido está pendiente ya que se ha creado
        this.estado = EstadoPedido.PENDIENTE;
    }

    //getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
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

    public List<LineaPedido> getLineasPedido() {
        return lineasPedido;
    }

    public void setLineasPedido(List<LineaPedido> lineasPedido) {
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

    //poner hascode y equals porque en otras clases tengo Set de pedido
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
