package org.example.model.descuento;

import org.example.model.producto.Producto;

public class DescuentoPorcentaje implements Descuento {
    private float descuetnoporcentaje;

    //constructor
    public DescuentoPorcentaje(float descuetnoporcentaje) {
        this.descuetnoporcentaje = descuetnoporcentaje;
    }

    //getters y setters
    public float getDescuetnoporcentaje() {
        return descuetnoporcentaje;
    }

    public void setDescuetnoporcentaje(float descuetnoporcentaje) {
        this.descuetnoporcentaje = descuetnoporcentaje;
    }

    //método implementado
    @Override
    public double calcularMontoDecuento(Producto producto) {

    }
}
