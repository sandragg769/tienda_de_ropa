package org.example.controller;

import org.example.controller.dao.impl.JdbcProductoDAO;
import org.example.controller.dao.impl.JdbcUsuarioDAO;
import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ControladorUsuario {
    private final UsuarioDAO usuarioDAO = JdbcUsuarioDAO.getInstance();

    // LOGIN
    public Optional<Usuario> login(String email, String password) throws SQLException {
        Optional<Usuario> op = usuarioDAO.findByEmail(email);
        if (op.isEmpty()) return null;

        Usuario u = op.get();
        return u.getPassword().equals(password) ? Optional.of(u) : null;
    }

    // CRUD
    public void registrarUsuario(Usuario usuario) throws SQLException {
        usuarioDAO.save(usuario);
    }

    // obtener por ID
    public Optional<Usuario> obtenerPorId(long id) throws SQLException {
        return usuarioDAO.findById(id);
    }

    public Optional<Usuario> obtenerPorEmail(String email) throws SQLException {
        return usuarioDAO.findByEmail(email);
    }

    // obtener TODOS los usuarios
    public List<Usuario> obtenerTodos() throws SQLException {
        return usuarioDAO.findAll();
    }

    public boolean actualizarUsuario(Usuario usuario) throws SQLException {
        return usuarioDAO.update(usuario);

    }

    public boolean eliminarUsuario(long id) throws SQLException {
        return usuarioDAO.delete(id);

    }

    //FAVORITOS
    // obtener favoritos de un usuario
    public List<Producto> obtenerFavoritos(long usuarioId) throws SQLException {
        return usuarioDAO.findFavoritos(usuarioId);
    }

    public void agregarFavorito(Producto producto, Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() <= 0)
            throw new IllegalArgumentException("Usuario inválido.");
        if (producto == null || producto.getId() <= 0)
            throw new IllegalArgumentException("Producto inválido o sin persistir.");
        ProductoDAO productoDAO = JdbcProductoDAO.getInstance();
        productoDAO.agregarFavorito(usuario.getId(), producto.getId());
    }

    public void eliminarFavorito(Producto producto, Usuario usuario) throws SQLException {
        if (usuario == null || usuario.getId() <= 0)
            throw new IllegalArgumentException("Usuario inválido.");
        if (producto == null || producto.getId() <= 0)
            throw new IllegalArgumentException("Producto inválido o sin persistir.");
        ProductoDAO productoDAO = JdbcProductoDAO.getInstance();
        productoDAO.eliminarFavorito(usuario.getId(), producto.getId());
    }


    // NO TIENE GESTOR FICHEROS
}
