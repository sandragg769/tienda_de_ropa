package org.example.controller.dao.interfaces;

import org.example.model.Usuario;

import java.util.List;
import java.util.Optional;

//es una interfaz la cual implementaremos en otra clase
//se utiliza para separar y desacoplar la lógica de acceso a diferentes fuentes de datos como bases de datos de la lógica de negocio.
public interface UsuarioDAO {
    //CRUD
    boolean save(Usuario usuario);

    Optional<Usuario> findById(long id);

    List<Usuario> findAll();

    boolean update(Usuario usuario);

    boolean delete(long id);

    //operaciones adicionales
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findFavoritos(long usuarioId);
}
