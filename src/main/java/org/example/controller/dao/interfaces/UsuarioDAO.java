package org.example.controller.dao.interfaces;

import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

//es una interfaz la cual implementaremos en otra clase
//se utiliza para separar y desacoplar la lógica de acceso a diferentes fuentes de datos como bases de datos de la lógica de negocio.
public interface UsuarioDAO {
    //CRUD
    boolean save(Usuario usuario) throws SQLException;

    Optional<Usuario> findById(long id) throws SQLException;

    List<Usuario> findAll() throws SQLException;

    boolean update(Usuario usuario) throws SQLException;

    boolean delete(long id) throws SQLException;

    //operaciones adicionales
    Optional<Usuario> findByEmail(String email) throws SQLException;

    List<Producto> findFavoritos(long usuarioId) throws SQLException;


}
