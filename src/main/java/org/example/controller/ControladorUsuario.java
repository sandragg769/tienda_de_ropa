package org.example.controller;

import org.example.controller.dao.impl.JdbcUsuarioDAO;
import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import java.sql.SQLException;
import java.util.Optional;

public class ControladorUsuario {
    private final UsuarioDAO usuarioDAO = JdbcUsuarioDAO.getInstance();

    public void registrarUsuario(Usuario usuario) throws SQLException {
        usuarioDAO.save(usuario);
    }

    public Optional<Usuario> login(String email, String pass) throws SQLException {
        return usuarioDAO.findByEmail(email)
                .filter(u -> u.getPassword().equals(pass));
    }

}
