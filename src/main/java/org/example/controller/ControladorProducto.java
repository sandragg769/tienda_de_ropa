package org.example.controller;


import org.example.model.descuento.Descuento;
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
    private long contadorEtiquetas = 0;


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

    //metodo para buscar un producto por id
    public Producto buscarProductoPorId(long id) {
        for (Producto p : listaProductos) {
            if (p.getId() == id) return p;
        }
        throw new IllegalArgumentException("Producto no encontrado con id=" + id);
    }

    //metodo para eliminar productos de la lista
    public void eliminarUsuario(long id) {
        for (Producto producto : listaProductos) {
            //busca en la lista de registrados el id ya que al estar registrado tiene id y si lo encuentra lo elimina
            if (producto.getId() == id) {
                listaProductos.remove(producto);
                //si esto pasa que se salga de la lista, si no se pone siempre da exception de abajo
                return;
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se puede eliminar el producto ya que no se encuentra ese Id.");
    }

    //metodo para actualizar producto, se cambia todo menos, id, los usuariosProductosFavoritos y la linea de pedido
    public Producto actualizarProducto(Producto productoNuevo) {
        for (Producto p : listaProductos) {
            // busca en la lista por id
            if (p.getId() == productoNuevo.getId()) {
                // NO CAMBIAR ID
                if (productoNuevo.getId() != p.getId()) {
                    throw new IllegalArgumentException("No se puede cambiar el ID de un producto.");
                }

                // NO CAMBIAR usuariosProductosFavoritos
                if (!p.getUsuariosProductosFavoritos().equals(productoNuevo.getUsuariosProductosFavoritos())) {
                    throw new IllegalArgumentException("No se pueden modificar los usuarios que tienen el producto como favorito desde aquí.");
                }

                // NO CAMBIAR lineasPedido
                if (!p.getLineaPedido().equals(productoNuevo.getLineaPedido())) {
                    throw new IllegalArgumentException("No se pueden modificar las líneas de pedido desde aquí.");
                }

                // Sí se pueden cambiar las propiedades básicas
                p.setNombre(productoNuevo.getNombre());
                p.setMarca(productoNuevo.getMarca());
                p.setPrecioInicial(productoNuevo.getPrecioInicial());
                p.setTalla(productoNuevo.getTalla());
                p.setColor(productoNuevo.getColor());
                p.setEtiqueta(productoNuevo.getEtiqueta());
                p.setDescuento(productoNuevo.getDescuento());

                return p;
            }
        }
        // si no encuentra el producto
        throw new IllegalArgumentException("No se puede actualizar el producto: ID no encontrado.");
    }

    //METODOS QUE TIENEN QUE VER CON DESCUENTO
    //cambiarle el descuento especificamente al producto
    public void asignarDescuento(long productoId, Descuento descuento) {
        Producto p = buscarProductoPorId(productoId);
        p.setDescuento(descuento);
    }

    //quitar ese descuento poniendolo null
    public void eliminarDescuento(long productoId) {
        Producto p = buscarProductoPorId(productoId);
        p.setDescuento(null);
    }

    //METODO PARA CREAR ETIQUETA (no hace falta el crud)
    public void asignarEtiqueta(long productoId, Etiqueta etiqueta) {
        Producto p = buscarProductoPorId(productoId);
        p.setEtiqueta(etiqueta);
    }







}
