package org.example.controller.dao.interfaces;

import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductoDAO {
    Producto save(Producto producto) throws SQLException;

    Optional<Producto> findById(long id) throws SQLException;

    List<Producto> findAll() throws SQLException;

    void update(Producto producto) throws SQLException;

    void delete(long id) throws SQLException;

    // Extra del enunciado
    List<Usuario> findUsuariosFavoritos(long productoId) throws SQLException;

    List<Usuario> agregarFavorito(long productoId, long usuarioId) throws SQLException;

    void eliminarFavorito(long productoId, long usuarioId) throws SQLException;
}
