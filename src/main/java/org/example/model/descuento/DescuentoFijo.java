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
        double precio = producto.getPrecioInicial();

        // No puede superar el 80% del precio inicial
        if (descuentoFijo > precio * 0.8) {
            throw new IllegalArgumentException(
                    "El descuento fijo no puede superar el 80% del precio del producto."
            );
        }
        //
        return descuentoFijo;
    }
}
