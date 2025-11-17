package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//implementación del DAO, aquí es donde se debe utilizar la tecnología específica de base de datos como JDBC
public class JdbcUsuarioDAO implements UsuarioDAO {
    //metodo de insertar usuario a la base de datos
    @Override
    public boolean save(Usuario usuario) throws  SQLException{
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

        }
        // devolver falso si no se ha podido hacer
        return false;
    }

    //metodo auxiliar (porque se repite mucho) para pasar de lo obtenido de la consulta a un objeto java, hacer a mano porque JDBC no lo hace
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getString("dni"),
                rs.getString("direccion"),
                rs.getDate("fecha_nacimiento").toLocalDate(),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("password")
        );
        usuario.setId(rs.getLong("id"));
        usuario.setId(rs.getLong("id"));
        usuario.setDni(rs.getString("dni"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setDireccion(rs.getString("direccion"));
        usuario.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasssword(rs.getString("password"));
        return usuario;
    }

    //metodo para encontrar un usuario mediante su id
    @Override
    public Optional<Usuario> findById(long id) {
        String sentencia = "SELECT * FROM usuario WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            //poner el valor del '?' y ponerlo como id (que es lo que buscamos)
            pstmt.setLong(1, id);
            //ejecutar sentencia
            ResultSet rs = pstmt.executeQuery();

            //mapear al usuario a java de lo obtenido en la consulta
            if (rs.next()) {
                Usuario usuario = mapearUsuario(rs);
                //usar Optional por si no lo encuentra
                return Optional.of(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por id: " + e.getMessage());
        }
        //si no obtiene nada devolver un optional vacío
        return Optional.empty();
    }

    //metodo que devuelve todos los usuarios
    @Override
    public List<Usuario> findAll() {
        List<Usuario> lista = new ArrayList<>();
        String sentencia = "SELECT * FROM usuario";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             //no se necesita prepared ya que no se pasa ningún dato simplemente se obtienen todos los usuarios
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sentencia)) {

            //añadir a la lista a devolver los usuarios (mapeados para que java o entienda
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
        }

        return lista;
    }

    //metodo para actualizar un usuario
    @Override
    public boolean update(Usuario usuario) {
        String sentencia = "UPDATE usuario SET nombre=?, direccion=?, telefono=?, email=?, password=? WHERE id=?";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            //pasamos a java los parametros, hacemos un set para cambiarlo
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getDireccion());
            pstmt.setString(3, usuario.getTelefono());
            pstmt.setString(4, usuario.getEmail());
            pstmt.setString(5, usuario.getPasssword());
            pstmt.setLong(6, usuario.getId());

            // ejecutamos la consulta
            int filas = pstmt.executeUpdate();
            //si da 0 líneas devuelve false, si da alguna linea es true (está correcto)
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }

        //si no se conecta da false pq no se hace
        return false;
    }

    //metodo para borrar un usuario
    @Override
    public boolean delete(long id) {
        String sentencia = "DELETE FROM usuario WHERE id=?";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            //asignar el parametro
            pstmt.setLong(1, id);
            //ejecutar
            int filas = pstmt.executeUpdate();
            //si devuelve alguna linea es true si  devuelve 0 es false
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }

        return false;
    }

    //metodo para buscar un usuario por email
    @Override
    public Optional<Usuario> findByEmail(String email) {
        String sentencia = "SELECT * FROM usuario WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                //mapear para que lo pille java y sepa que devolver
                return Optional.of(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por email: " + e.getMessage());
        }

        return Optional.empty();
    }

    //metodo se obtienen los productos favoritos de un usuario
    @Override
    public List<Producto> findFavoritos(long usuarioId) {
        List<Producto> productosFavoritos = new ArrayList<>();
        String sentencia = "SELECT p.* FROM producto p " +
                "JOIN usuario_producto_favorito upf ON p.id = upf.producto_id " +
                "WHERE upf.usuario_id = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Producto producto = mapearProducto(rs);
                productosFavoritos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos favoritos del usuario: " + e.getMessage());
        }

        return productosFavoritos;
    }
}
