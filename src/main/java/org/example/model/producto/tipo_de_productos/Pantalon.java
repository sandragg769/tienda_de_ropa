package org.example.model.producto.tipo_de_productos;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Talla;

// marca la clase como una entidad JPA (se persistirá en la base de datos)
@Entity
// columna que JPA usará para saber que subclase concreta es cada fila
@DiscriminatorValue("Pantalon")
public class Pantalon extends Producto {
    // campo opcional
    private int botones;

    // constructor vacío obligatorio para JPA
    public Pantalon() {

    }

    // constructor
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
