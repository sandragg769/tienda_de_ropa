package org.example.model.descuento;

import org.example.model.producto.Producto;

public class DescuentoFijo implements Descuento {
    private float descuentoFijo;

    //constructor
    public DescuentoFijo(float descuentoFijo) {
        this.descuentoFijo = descuentoFijo;
    }

    //getters y setters
    public float getDescuentoFijo() {
        return descuentoFijo;
    }

    public void setDescuentoFijo(float descuentoFijo) {
        this.descuentoFijo = descuentoFijo;
    }

    @Override
    public double calcularMontoDecuento(Producto producto) {

    }
}
