package org.example.model.pedido;

import jakarta.persistence.*;
import org.example.model.producto.Producto;

// marca la clase como una entidad JPA (se persistirá en la base de datos)
@Entity
// nombre de la tabla en la base de datos
@Table(name = "linea_pedido")
public class LineaPedido {
    // identificador único de la entidad
    @Id
    // el valor del ID se genera automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int cantidad;

    // muchas líneas de pedido pueden referenciar al mismo producto
    // lazy: el producto no se carga hasta que se accede explícitamente
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto producto;

    // muchas líneas pertenecen a un mismo pedido
    // lazy: el producto no se carga hasta que se accede explícitamente
    @ManyToOne(fetch = FetchType.LAZY)
    private Pedido pedido;


    // constructor vacío obligatorio para JPA
    public LineaPedido() {
    }

    // constructor
    // aquí sí hay que poner un Producto obligatoriamente
    public LineaPedido(int cantidad, Producto producto, Pedido pedido) {
        this.cantidad = cantidad;
        this.producto = producto;
        this.pedido = pedido;
    }


    // getters y setters
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

    // metodo para calcular el precio de una línea de pedido
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
