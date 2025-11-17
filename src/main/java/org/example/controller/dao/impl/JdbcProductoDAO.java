package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcProductoDAO implements ProductoDAO {
    private static volatile JdbcProductoDAO instance;

    private JdbcProductoDAO() {
    }

    public static JdbcProductoDAO getInstance() {
        if (instance == null) {
            synchronized (JdbcProductoDAO.class) {
                if (instance == null) instance = new JdbcProductoDAO();
            }
        }
        return instance;
    }

    @Override
    public Producto save(Producto producto) throws SQLException {
        return null;
    }

    @Override
    public Optional<Producto> findById(long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Producto> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public void update(Producto producto) throws SQLException {

    }

    @Override
    public void delete(long id) throws SQLException {

    }

    @Override
    public List<Usuario> findUsuariosFavoritos(long productoId) throws SQLException {
        return List.of();
    }

    @Override
    public void agregarFavorito(long productoId, long usuarioId) throws SQLException {

    }

    @Override
    public void eliminarFavorito(long productoId, long usuarioId) throws SQLException {

    }
}
