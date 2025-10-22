package org.example.model.producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.model.Usuario;
import org.example.model.descuento.Descuento;
import org.example.model.pedido.LineaPedido;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Chaqueta;
import org.example.model.producto.tipo_de_productos.Pantalon;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

//por JSON
//al haber herencia hay que poner esto
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Camisa.class, name = "Camisa"),
        @JsonSubTypes.Type(value = Pantalon.class, name = "Pantalon"),
        @JsonSubTypes.Type(value = Chaqueta.class, name = "Chaqueta")
})

//clase que usaremos de plantilla para los diferentes productos
public abstract class Producto {
    //no id en JSON
    @JsonIgnore
    private long id;
    private String nombre;
    private String marca;
    private double precioInicial;
    private Talla talla;
    private Color color;
    //solo puede tener una etiqueta por eso no se hace lista
    private Etiqueta etiqueta;
    @JsonIgnore
    //un set porque no hay usuarios repetidos, representa los usuarios que han puesto el producto en favoritos (que pueden ser muchos)
    private transient Set<Usuario> usuariosProductosFavoritos = new HashSet<>();
    @JsonIgnore
    //un producto puede tener una línea de pedido (la línea tiene id por lo que no se refiere en general, si no que a esa especifica)
    private transient LineaPedido lineaPedido;
    //un producto puede tener 0 o 1 descuentos
    private Descuento descuento;

    //constructor vacío para la exportación e importación de JSON
    public Producto() {
    }

    //constructor
    //no id
    protected Producto(String nombre, String marca, double precioInicial, Talla talla, Color color,
                       Etiqueta etiqueta) {
        this.nombre = nombre;
        this.marca = marca;
        this.precioInicial = precioInicial;
        this.talla = talla;
        this.color = color;
        this.etiqueta = etiqueta;
        //no poner lineaPedido ni usuarioFavoritos
        //no ponemos descuento para empezar, después si queremos usamos el setter del descuento
        this.descuento = null;
    }

    //getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public double getPrecioInicial() {
        return precioInicial;
    }

    public void setPrecioInicial(double precioInicial) {
        this.precioInicial = precioInicial;
    }

    public Talla getTalla() {
        return talla;
    }

    public void setTalla(Talla talla) {
        this.talla = talla;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Etiqueta getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(Etiqueta etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Set<Usuario> getUsuariosProductosFavoritos() {
        return usuariosProductosFavoritos;
    }

    public void setUsuariosProductosFavoritos(Set<Usuario> usuariosProductosFavoritos) {
        this.usuariosProductosFavoritos = usuariosProductosFavoritos;
    }

    public LineaPedido getLineaPedido() {
        return lineaPedido;
    }

    public void setLineaPedido(LineaPedido lineaPedido) {
        this.lineaPedido = lineaPedido;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public void setDescuento(Descuento descuento) {
        this.descuento = descuento;
    }

    //no atributos calculados en JSON
    @JsonIgnore
    //metodo para obtener el precio final del producto, con descuento aplicado (restar el dinero que me da el metodo del descuento al del precioInicial
    public double getPrecioFinal() {
        double descuentoAAplicar;
        if (descuento == null) {
            // si no tiene descuento no cambia el precioInicial
            return precioInicial;
        } else {
            //gracias al polimorfismo sabe cuál tiene y podemos ponerlo generalmente
            //se pone el this pq se refiere a el Producto (hay que pasarle un producto)
            descuentoAAplicar = descuento.calcularMontoDescuento(this);
            //se calcula el precio con descuento
            return precioInicial - descuentoAAplicar;
        }
    }

    //hasCode y equals ya que tenemos Set, si tienen el mismo id son iguales
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return id == producto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
