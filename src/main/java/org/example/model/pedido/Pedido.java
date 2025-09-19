package org.example.model.pedido;

import java.util.Date;

public class Pedido {
    private long id;
    private Date fecha;
    private EstadoPedido estrado;

    //constructor
    public Pedido(long id, Date fecha, EstadoPedido estrado) {
        this.id = id;
        this.fecha = fecha;
        this.estrado = estrado;
    }

    //getters
    public long getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    public EstadoPedido getEstrado() {
        return estrado;
    }

    //getters
    public void setId(long id) {
        this.id = id;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setEstrado(EstadoPedido estrado) {
        this.estrado = estrado;
    }

    //Este método nos devuelve la suma de las líneas del pedido
    public double getPecioTotal() {

    }
}
