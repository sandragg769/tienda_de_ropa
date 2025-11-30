package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// implementación del DAO, aquí es donde se debe utilizar la tecnología específica de base de datos como JDBC
public class JdbcUsuarioDAO implements UsuarioDAO {

    // usar singleton en los dao, sirve para que solo haya una instancia de una clase en toda la aplicacion, es un punto global de acceso a esa instancia
    // se usa en los dao para que solo haya una implementación que gestione el acceso a la base de datos
    // referencia estática
    private static volatile JdbcUsuarioDAO instance;

    // constructor privado
    private JdbcUsuarioDAO() {
    }

    // punto de acceso
    public static JdbcUsuarioDAO getInstance() {
        if (instance == null) {
            synchronized (JdbcUsuarioDAO.class) {
                if (instance == null) {
                    //crea instancia por primera vez
                    instance = new JdbcUsuarioDAO();
                }
            }
        }
        return instance;
    }

    public static void resetForTests() {
        instance = new JdbcUsuarioDAO();
    }

    // CRUD
    // metodo de insertar usuario a la base de datos
    @Override
    public boolean save(Usuario usuario) throws SQLException {
        //el id se autoincrementa sin tener que ponerlo
        String sentencia = "INSERT INTO usuario (dni, nombre, direccion, fecha_nacimiento, telefono, email, password) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // establecemos conexión con la base de datos
        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             // prepared statement para parametrizar la consulta, evitar la inyección de código, se necesita este y no statement ya que
             // ponemos en la consulta datos que puede ser peligrosos poner
             // el segundo parámetro indica que queremos recuperar las claves generales del motor (el id auto-increment)
             PreparedStatement pstmt = connection.prepareStatement(sentencia, Statement.RETURN_GENERATED_KEYS)) {

            // asignar las '?' con lo que corresponde
            pstmt.setString(1, usuario.getDni());
            pstmt.setString(2, usuario.getNombre());
            pstmt.setString(3, usuario.getDireccion());
            pstmt.setDate(4, Date.valueOf(usuario.getFechaNacimiento()));
            pstmt.setString(5, usuario.getTelefono());
            pstmt.setString(6, usuario.getEmail());
            pstmt.setString(7, usuario.getPassword());

            // ejecutamos la sentencia guardando el número de filas afectadas
            int filas = pstmt.executeUpdate();

            // si ha afectado a alguna fila (ha funcionado)
            if (filas > 0) {
                // reuperamos el id generado pidiendo las claves generadas por la inserción
                // se cierra automáticamente
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        //obtenemos la priemra columna y le asignamos el id al usuario
                        usuario.setId(rs.getLong(1));
                    }
                }
                // devuelve true si ha salido bien
                return true;
            }

        }
        // devolver falso si no se ha podido hacer
        return false;
    }

    // metodo auxiliar (porque se repite mucho) para pasar de lo obtenido de la consulta a un objeto java, hacer a mano porque JDBC no lo hace
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario(
                rs.getString("nombre"),
                rs.getString("dni"),
                rs.getString("direccion"),
                rs.getDate("fecha_nacimiento").toLocalDate(),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("password")
        );
        u.setId(rs.getLong("id"));
        return u;
    }

    // metodo PÚBLICO que se podrá usar en otra clase, no se puedo poner el mapeo público directamente
    // porque se rompen los principios del dao, pero si se puede hacer esto porque no se exponen los detalles internos
    public Usuario mapearUsuarioPublic(ResultSet rs) throws SQLException {
        return mapearUsuario(rs);
    }

    // metodo para encontrar un usuario mediante su id
    @Override
    public Optional<Usuario> findById(long id) throws SQLException {
        String sentencia = "SELECT * FROM usuario WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            // poner el valor del '?' y ponerlo como id (que es lo que buscamos)
            pstmt.setLong(1, id);
            // ejecutar sentencia
            ResultSet rs = pstmt.executeQuery();

            // mapear al usuario a java de lo obtenido en la consulta
            if (rs.next()) {
                Usuario usuario = mapearUsuario(rs);
                // usar Optional por si no lo encuentra
                return Optional.of(usuario);
            }

        }
        //si no obtiene nada devolver un optional vacío
        return Optional.empty();
    }

    // metodo que devuelve todos los usuarios
    @Override
    public List<Usuario> findAll() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sentencia = "SELECT * FROM usuario";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             // no se necesita prepared ya que no se pasa ningún dato simplemente se obtienen todos los usuarios
             Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sentencia)) {

            // añadir a la lista a devolver los usuarios (mapeados para que java o entienda
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }

        }

        return lista;
    }

    // metodo para actualizar un usuario
    @Override
    public boolean update(Usuario usuario) throws SQLException {
        String sentencia = "UPDATE usuario SET nombre=?, direccion=?, telefono=?, email=?, password=? WHERE id=?";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            //pasamos a java los parametros, hacemos un set para cambiarlo
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getDireccion());
            pstmt.setString(3, usuario.getTelefono());
            pstmt.setString(4, usuario.getEmail());
            pstmt.setString(5, usuario.getPassword());
            pstmt.setLong(6, usuario.getId());

            // ejecutamos la consulta
            int filas = pstmt.executeUpdate();
            // si da 0 líneas devuelve false, si da alguna linea es true (está correcto)
            return filas > 0;

        }
    }

    // metodo para borrar un usuario
    @Override
    public boolean delete(long id) throws SQLException {
        String sentencia = "DELETE FROM usuario WHERE id=?";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            // asignar el parametro
            pstmt.setLong(1, id);
            // ejecutar
            int filas = pstmt.executeUpdate();
            // si devuelve alguna linea es true si  devuelve 0 es false
            return filas > 0;

        }
    }


    // METODOS ESPECÍFICOS
    // metodo para buscar un usuario por email
    @Override
    public Optional<Usuario> findByEmail(String email) throws SQLException {
        String sentencia = "SELECT * FROM usuario WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // mapear para que lo pille java y sepa que devolver
                return Optional.of(mapearUsuario(rs));
            }

        }

        return Optional.empty();
    }

    // metodo se obtienen los productos favoritos de un usuario
    @Override
    public List<Producto> findFavoritos(long usuarioId) throws SQLException {
        List<Producto> productosFavoritos = new ArrayList<>();
        String sentencia = "SELECT p.*, " +
                "e.id AS etiqueta_id, " +
                "e.nombre AS etiqueta_nombre, " +
                "e.fecha_creacion AS etiqueta_fecha_creacion " +
                "FROM producto p " +
                "JOIN usuario_producto_favorito upf ON p.id = upf.producto_id " +
                "LEFT JOIN etiqueta e ON p.etiqueta_id = e.id " +
                "WHERE upf.usuario_id = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = connection.prepareStatement(sentencia)) {

            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // usar el public ya que si no no deja
                Producto producto = JdbcProductoDAO.getInstance().mapearProductoPublic(rs);
                productosFavoritos.add(producto);
            }

        }
        return productosFavoritos;
    }
}

