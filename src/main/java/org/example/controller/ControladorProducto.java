package org.example.controller;


import org.example.controller.dao.impl.JdbcProductoDAO;
import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;
import org.example.utils.GestorFicherosCSV;
import org.example.utils.GestorFicherosJSON;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ControladorProducto {
    private final ProductoDAO productoDAO = JdbcProductoDAO.getInstance();


    // CRUD
    //guardar producto (con su etiqueta si la tiene)
    public void crearProducto(Producto producto) throws SQLException {
        productoDAO.save(producto);
    }

    public Optional<Producto> buscarPorId(long id) throws SQLException {
        return productoDAO.findById(id);
    }

    public List<Producto> obtenerTodos() throws SQLException {
        return productoDAO.findAll();
    }

    public void actualizarProducto(Producto producto) throws SQLException {
        productoDAO.update(producto);
    }

    public void borrarProducto(long id) throws SQLException {
        productoDAO.delete(id);
    }


    // FAVORITOS
    public List<Usuario> obtenerUsuariosFavoritoDeProducto(long productoId) throws SQLException {
        return productoDAO.findUsuariosFavoritos(productoId);
    }

    public void agregarFavorito(long productoId, long usuarioId) throws SQLException {
        productoDAO.agregarFavorito(productoId, usuarioId);
    }

    public void eliminarFavorito(long productoId, long usuarioId) throws SQLException {
        productoDAO.eliminarFavorito(productoId, usuarioId);
    }


    // GESTOR FICHEROS PRODUCTOS
    // exportar objetos producto a fichero JSON
    public void exportarProductosAJSON() throws SQLException {
        List<Producto> listaProductos = productoDAO.findAll();
        String rutaFichero = "productos.json";
        GestorFicherosJSON.exportarProductosAJSON(listaProductos, rutaFichero);
    }

    // importar JSON a objetos producto
    public void importarProductosDesdeJSON() {
        String rutaFichero = "productos.json";
        List<Producto> importados = GestorFicherosJSON.importarProductosDesdeJSON(rutaFichero);
        listaProductos.clear();
        listaProductos.addAll(importados);
    }


    // GESTOR FICHEROS ETIQUETAS
    public void exportarEtiquetasCSV() throws SQLException {
        List<Producto>  =productoDAO.findAll();
        GestorFicherosCSV.exportarEtiquetasACSV(listaEtiquetas, "etiquetas.csv");
    }

    public void importarEtiquetasCSV() {
        listaEtiquetas = GestorFicherosCSV.importarEtiquetasDesdeCSV("etiquetas.csv");
    }


}
