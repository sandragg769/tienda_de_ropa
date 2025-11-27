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
    private final UsuarioDAO usuarioDAO;

    public ControladorUsuario() {
        this.usuarioDAO = JdbcUsuarioDAO.getInstance();
    }

    // LOGIN
    public Optional<Usuario> login(String email, String password) throws SQLException {
        Usuario usuario = usuarioDAO.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email no registrado"));

        if (!usuario.getPassword().equals(password)) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        return Optional.of(usuario);
    }

    // CRUD
    public void registrarUsuario(Usuario usuario) throws SQLException {
        usuarioDAO.save(usuario);
    }

    // obtener por ID
    public Optional<Usuario> obtenerPorId(long id) throws SQLException {
        return Optional.ofNullable(usuarioDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id " + id)));
    }

    public Usuario obtenerPorEmail(String email) throws SQLException {
        return usuarioDAO.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe usuario con email: " + email));
    }

    // obtener TODOS los usuarios
    public List<Usuario> obtenerTodos() throws SQLException {
        return usuarioDAO.findAll();
    }

    public void actualizarUsuario(Usuario usuario) throws SQLException {
        // comprobar que el usuario existe
        usuarioDAO.findById(usuario.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("No existe un usuario con id: " + usuario.getId()));
        // comprobar que campos no modificables NO han cambiado: DNI
        Usuario original = usuarioDAO.findById(usuario.getId()).get();
        if (!original.getDni().equals(usuario.getDni())) {
            throw new IllegalArgumentException("El DNI no se puede modificar");
        }
        // si tod es correcto → actualizar
        usuarioDAO.update(usuario);
    }

    public boolean eliminarUsuario(long id) throws SQLException {
        boolean eliminado = usuarioDAO.delete(id);

        if (!eliminado) {
            throw new IllegalArgumentException("No existe usuario con id: " + id);


        }
        // solo llega aquí si se eliminó correctamente
        return true;
    }

    //FAVORITOS
    // obtener favoritos de un usuario
    public List<Producto> obtenerFavoritos(long usuarioId) throws SQLException {
        // comprobamos existencia
        usuarioDAO.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("No existe usuario con id: " + usuarioId));

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
