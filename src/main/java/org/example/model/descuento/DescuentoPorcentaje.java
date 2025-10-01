package org.example.model.descuento;

import org.example.model.producto.Producto;

public class DescuentoPorcentaje implements Descuento {
    private float descuentoPorcentaje;

    //constructor (comprobar que no sea descuento negativo, si lo es lo igual a 0)
    public DescuentoPorcentaje(float porcentaje) {
        if (porcentaje < 0) porcentaje = 0;
        this.descuentoPorcentaje = porcentaje;
    }

    //getters y setters
    public float getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(float descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    //metodo implementado
    //si no hay producto devuelve 0, si hay, calcula el dinero a restar para que se aplique el descuento
    @Override
    public double calcularMontoDescuento(Producto producto) {
        //si no paso producto que no quite nada
        if (producto == null) return 0;
        //devuelve el dinero que después tengo que descontar al precio
        return producto.getPrecioInicial() * (descuentoPorcentaje / 100.0);
    }
}
