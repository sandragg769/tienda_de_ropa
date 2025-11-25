package org.example.controller;

import org.example.controller.dao.impl.JdbcUsuarioDAO;
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

    // NO TIENE GESTOR FICHEROS
}
