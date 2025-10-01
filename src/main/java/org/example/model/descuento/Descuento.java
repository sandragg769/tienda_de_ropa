package org.example.model.descuento;

import org.example.model.producto.Producto;

public interface Descuento {
    //metodo a implementar en las otras clases de descuento
    //relación con producto (parámetro) no deja poner varios descuentos a un producto??
    double calcularMontoDescuento(Producto producto);
}
