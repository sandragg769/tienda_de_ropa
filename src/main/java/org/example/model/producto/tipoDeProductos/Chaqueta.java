package org.example.model.producto.tipoDeProductos;

import org.example.model.producto.Etiqueta;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Talla;

public class Chaqueta extends Producto {
    private boolean conCapucha;
    private int nivelAbrigo;

    //constructor
    public Chaqueta(long id, String nombre, String marca, double precioInicial, Talla talla,
                    Color color, Etiqueta etiqueta, boolean conCapucha, int nivelAbrigo) {
        super(id, nombre, marca, precioInicial, talla, color, etiqueta);
        this.conCapucha = conCapucha;
        this.nivelAbrigo = nivelAbrigo;
    }

    //getters y setters
    public boolean isConCapucha() {
        return conCapucha;
    }

    public void setConCapucha(boolean conCapucha) {
        this.conCapucha = conCapucha;
    }

    public int getNivelAbrigo() {
        return nivelAbrigo;
    }

    public void setNivelAbrigo(int nivelAbrigo) {
        this.nivelAbrigo = nivelAbrigo;
    }
}
