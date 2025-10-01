package org.example.model.producto.tipo_de_productos;

import org.example.model.producto.Etiqueta;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Talla;

public class Pantalon extends Producto {
    private int botones;

    //constructor
    //pedir con super los datos del Producto y después los atributos del producto en si
    public Pantalon(String nombre, String marca, double precioInicial, Talla talla,
                    Color color, Etiqueta etiqueta, int botones) {
        super(nombre, marca, precioInicial, talla, color, etiqueta);
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
