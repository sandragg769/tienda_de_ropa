package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.util.List;
import java.util.Optional;

//implementación del DAO, aquí es donde se debe utilizar la tecnología específica de base de datos como JDBC
public class JdbcUsuarioDAO implements UsuarioDAO {
    //metodo de insertar usuario a la base de datos
    @Override
    public boolean save(Usuario usuario) {
        //el id se autoincrementa sin tener que ponerlo
        String sentencia = "INSERT INTO usuario (dni, nombre, direccion, fecha_nacimiento, telefono, email, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        //establecemos conexión con la base de datos
        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             //prepared statement para parametrizar la consulta, evitar la inyección de código, se necesita este y no statement ya que
             // ponemos en la consulta datos que puede ser peligrosos poner
             //el segundo parámetro indica que queremos recuperar las claves generales del motor (el id auto-increment)
             PreparedStatement pstmt = connection.prepareStatement(sentencia, Statement.RETURN_GENERATED_KEYS)) {

            //asignar las '?' con lo que corresponde
            pstmt.setString(1, usuario.getDni());
            pstmt.setString(2, usuario.getNombre());
            pstmt.setString(3, usuario.getDireccion());
            pstmt.setDate(4, Date.valueOf(usuario.getFechaNacimiento()));
            pstmt.setString(5, usuario.getTelefono());
            pstmt.setString(6, usuario.getEmail());
            pstmt.setString(7, usuario.getPasssword());

            //ejecutamos la sentencia guardando el número de filas afectadas
            int filas = pstmt.executeUpdate();

            //si ha afectado a alguna fila (ha funcionado)
            if (filas > 0) {
                //reuperamos el id generado pidiendo las claves generadas por la inserción
                //se cierra automáticamente
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        //obtenemos la priemra columna y le asignamos el id al usuario
                        usuario.setId(rs.getLong(1));
                    }
                }
                //devuelve true si ha salido bien
                return true;
            }

            //capturar exception si algo no sale bien
        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
        }
        // devolver falso si no se ha podido hacer
        return false;
    }

    //metodo pars encontrar un usuario mediante su id
    @Override
    public Optional<Usuario> findById(long id) {
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        return List.of();
    }

    @Override
    public boolean update(Usuario usuario) {
        return false;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public List<Usuario> findFavoritos(long usuarioId) {
        return List.of();
    }
}
