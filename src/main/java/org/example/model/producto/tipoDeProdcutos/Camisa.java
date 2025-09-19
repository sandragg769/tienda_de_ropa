package org.example.model.producto.tipoDeProdcutos;

import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Talla;

public class Camisa extends Producto {
    private int botones;

    //constructor
    public Camisa(long id, String nombre, String marca, double precioInicial, Talla talla, Color color, int botones) {
        super(id, nombre, marca, precioInicial, talla, color);
        this.botones = botones;
    }

    //getters y setters
    public int getBotones() {
        return botones;
    }

    public void setBotones(int botones) {
        this.botones = botones;
    }
}
