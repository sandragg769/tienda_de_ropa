package org.example.model.producto;

import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;

//clase que usaremos de plantilla para los diferentes productos
public abstract class Producto {
    private long id;
    private String nombre;
    private String marca;
    private double precioInicial;
    private Talla talla;
    private Color color;

    public Producto(long id, String nombre, String marca, double
            precioInicial, Talla talla, Color color) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.precioInicial = precioInicial;
        this.talla = talla;
        this.color = color;
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
