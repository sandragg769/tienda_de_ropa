package org.example.model.producto.tipoDeProdcutos;

import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Talla;

public class Chaqueta extends Producto {
    private boolean conCapucha;
    private int nuvelAbrigo;

    //constructor

    public Chaqueta(long id, String nombre, String marca, double precioInicial,
                    Talla talla, Color color, boolean conCapucha, int nuvelAbrigo) {
        super(id, nombre, marca, precioInicial, talla, color);
        this.conCapucha = conCapucha;
        this.nuvelAbrigo = nuvelAbrigo;
    }

    //getters y setters

    public boolean isConCapucha() {
        return conCapucha;
    }

    public void setConCapucha(boolean conCapucha) {
        this.conCapucha = conCapucha;
    }

    public int getNuvelAbrigo() {
        return nuvelAbrigo;
    }

    public void setNuvelAbrigo(int nuvelAbrigo) {
        this.nuvelAbrigo = nuvelAbrigo;
    }
}
