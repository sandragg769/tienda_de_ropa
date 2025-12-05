package org.example.model.producto.tipo_de_productos;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Talla;

@Entity
@DiscriminatorValue("CAMISA")
public class Camisa extends Producto {
    // opcional
    private int botones;

    //constructor
    //vacío para JSON
    public Camisa() {
    }

    public Camisa(String nombre, String marca, double precioInicial, Talla talla,
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
