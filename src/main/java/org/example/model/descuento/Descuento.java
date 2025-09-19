package org.example.model.descuento;

import org.example.model.producto.Producto;

public interface Descuento {
    //método a implementar en las otras clases de descuento
    public double calcularMontoDecuento(Producto producto);
}
