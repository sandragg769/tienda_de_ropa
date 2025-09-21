package org.example.model.pedido;

import org.example.model.producto.Producto;

public class LineaPedido {
    private long id;
    private int cantidad;
    //una línea de producto puede tener solo un pedido
    private Producto producto;

    //constructor
    //aquí sí hay que poner un Producto obligatoriamente
    public LineaPedido(long id, int cantidad, Producto producto) {
        this.id = id;
        this.cantidad = cantidad;
        this.producto = producto;
    }

    //getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    //metodo para calcular el precio de una línea de pedido
    // calculamos el precio final del producto (este anteriormente ya tiene en
    // cuenta el descuento) y lo multiplicamos por la cantidad del producto
    public double getPrecioSubTotal() {
        return producto.getPrecioFinal() * cantidad;
    }
}
