package org.example.model.descuento;

import org.example.model.producto.Producto;

public interface Descuento {
    //metodo a implementar en las otras clases de descuento
    //relacion con producto (parametro) no deja poner varios descuentos a un producto??
    public double calcularMontoDecuento(Producto producto);
}
