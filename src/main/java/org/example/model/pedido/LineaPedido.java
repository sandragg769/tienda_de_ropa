package org.example.model.pedido;

import jakarta.persistence.*;
import org.example.model.producto.Producto;

@Entity
@Table(name = "linea_pedido")
public class LineaPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int cantidad;

    //una línea de producto puede tener solo un pedido
    @ManyToOne(fetch = FetchType.EAGER)
    private Producto producto;

    //añadir pedido
    @ManyToOne(fetch = FetchType.EAGER)
    private Pedido pedido;

    //constructor

    public LineaPedido() {
    }

    //aquí sí hay que poner un Producto obligatoriamente
    public LineaPedido(int cantidad, Producto producto, Pedido pedido) {
        this.cantidad = cantidad;
        this.producto = producto;
        this.pedido = pedido;
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

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
