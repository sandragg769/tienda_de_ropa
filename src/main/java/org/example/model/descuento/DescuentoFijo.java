package org.example.model.descuento;

import org.example.model.producto.Producto;

public class DescuentoFijo implements Descuento {
    //la cantidad a restar del descuento
    private float descuentoFijo;

    //constructor (comprobar que no sea descuento negativo, si lo es lo iguala a 0)
    public DescuentoFijo(float descuentoFijo) {
        if (descuentoFijo < 0) descuentoFijo = 0;
        this.descuentoFijo = descuentoFijo;
    }

    //getters y setters
    public float getDescuentoFijo() {
        return descuentoFijo;
    }

    public void setDescuentoFijo(float descuentoFijo) {
        this.descuentoFijo = descuentoFijo;
    }

    //metodo implementado
    @Override
    public double calcularMontoDecuento(Producto producto) {
        //si no hay producto que devuelva 0 (para que no quite nada)
        if (producto == null) return 0;
        double precio = producto.getPrecioInicial();
        //no puede superar el 80% del precio inicial
        if (descuentoFijo > precio * 0.8) {
            throw new IllegalArgumentException(
                    "El descuento fijo no puede superar el 80% del precio del producto."
            );
        }
        //devuelve el dinero a descontar del precio del producto
        return descuentoFijo;
    }
}
