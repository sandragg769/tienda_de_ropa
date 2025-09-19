package org.example.model.pedido;

public class LineaPedido {
    private long id;
    private int cantidad;

    //constructor
    public LineaPedido(long id, int cantidad) {
        this.id = id;
        this.cantidad = cantidad;
    }

    //getters
    public long getId() {
        return id;
    }

    public int getCantidad() {
        return cantidad;
    }

    //setters
    public void setId(long id) {
        this.id = id;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioSubTotal () {

    }
}
