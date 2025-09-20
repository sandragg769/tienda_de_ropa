package org.example.model.producto;

import org.example.model.Usuario;
import org.example.model.descuento.Descuento;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.pedido.LineaPedido;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//clase que usaremos de plantilla para los diferentes productos
public abstract class Producto {
    private long id;
    private String nombre;
    private String marca;
    private double precioInicial;
    private Talla talla;
    private Color color;
    //solo puede tener una etiqueta por eso no se hace lista
    private Etiqueta etiqueta;
    //un set porque no hay usuarios repetidos, reperesenta los usuarios que han puesto el producto en favoritos (que pueden ser muchos)
    private Set<Usuario> usuariosProductosFavoritos = new HashSet<>();
    //un producto puede tener una linea de pedido (la linea tiene id por lo que no se refiere en general, si no que a esa especifica)
    private LineaPedido lineaPedido;
    //un producto puede tener 0 o 1 descuentos
    private Descuento descuento;

    public Producto(long id, String nombre, String marca, double
            precioInicial, Talla talla, Color color, Etiqueta etiqueta) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.precioInicial = precioInicial;
        this.talla = talla;
        this.color = color;
        //le pasamos una Etiqueta por parametro
        this.etiqueta = etiqueta;
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

    //método para obtener el precio final del producto, con descuento aplicado
    public double getPrecioFinal() {

    }

}
