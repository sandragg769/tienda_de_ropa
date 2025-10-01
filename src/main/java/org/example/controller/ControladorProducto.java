package org.example.controller;


import org.example.model.Usuario;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Chaqueta;
import org.example.model.producto.tipo_de_productos.Pantalon;

import java.util.ArrayList;
import java.util.List;

public class ControladorProducto {
    private List<Producto> listaProductos = new ArrayList<>();
    private long contadorProductos = 0;

    //METODOS CRUD PEDIDO
    public Producto crearCamisa(String nombre, String marca, double precioInicial,
                                Talla talla, Color color, Etiqueta etiqueta, int botones) {
        validarDatos(nombre, marca, precioInicial);
        Producto p = new Camisa(nombre, marca, precioInicial, talla, color, etiqueta, botones);
        p.setId(contadorProductos++);
        listaProductos.add(p);
        return p;
    }

    public Producto crearPantalon(String nombre, String marca, double precioInicial,
                                  Talla talla, Color color, Etiqueta etiqueta, int bolsillos) {
        validarDatos(nombre, marca, precioInicial);
        Producto p = new Pantalon(nombre, marca, precioInicial, talla, color, etiqueta, bolsillos);
        p.setId(contadorProductos++);
        listaProductos.add(p);
        return p;
    }

    public Producto crearChaqueta(String nombre, String marca, double precioInicial,
                                  Talla talla, Color color, Etiqueta etiqueta, boolean conCapucha, int nivelAbrigo) {
        validarDatos(nombre, marca, precioInicial);
        Producto p = new Chaqueta(nombre, marca, precioInicial, talla, color, etiqueta, conCapucha, nivelAbrigo);
        p.setId(contadorProductos++);
        listaProductos.add(p);
        return p;
    }

    //METODO AUXILIAR para validar los datos a la hora de crear objetos, lo pongo separado ya que al crear objetos de distintos tipos y con distintos atributos se repetiría tres veves el control
    private void validarDatos(String nombre, String marca, double precioInicial) {
        if (nombre == null || marca == null)
            throw new IllegalArgumentException("Nombre o marca no pueden ser nulos");
        if (precioInicial < 0)
            throw new IllegalArgumentException("Precio inicial negativo");
    }

    //metodo para leer productos
    public List<Producto> leerProductos() {
        return listaProductos;
    }


}
