package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.model.Usuario;
import org.example.model.descuento.Descuento;
import org.example.model.descuento.DescuentoFijo;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Chaqueta;
import org.example.model.producto.tipo_de_productos.Pantalon;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductoDAO implements ProductoDAO {
    //campor estático que contendrá la única instancia del DAO
    private static volatile JdbcProductoDAO instance;

    //constructor privado para evitar instanciación externa
    private JdbcProductoDAO() {
    }

    // punto de acceso al singleton
    public static JdbcProductoDAO getInstance() {
        //primera comprobación sin bloqueo
        if (instance == null) {
            //bloqueo por clase
            synchronized (JdbcProductoDAO.class) {
                // comprobación dentro del bloqueo
                if (instance == null) instance = new JdbcProductoDAO();
            }
        }
        return instance;
    }

    public static void resetForTests() {
        instance = null;
    }

    // CRUD
    //metodo que guarda un producto en la base de datos, si tiene etiqueta
    // sin id inserta la etiqueta primero, se hace en una transacción para
    // garantizar que no se cree solo una cosa
    @Override
    public Producto save(Producto producto) throws SQLException {
        String sentenciaEtiqueta =
                "INSERT INTO etiqueta (nombre, fecha_creacion) VALUES (?, ?)";
        String sentenciaProducto =
                "INSERT INTO producto (tipo, nombre, marca, precio_inicial, talla, color, etiqueta_id, " +
                        "descuento_tipo, descuento_valor, botones, bolsillos, con_capucha, nivel_abrigo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // validaciones básicas para evitar insertar datos inválidos
        if (producto == null) throw new IllegalArgumentException("Producto nulo");
        if (producto.getNombre() == null || producto.getMarca() == null)
            throw new IllegalArgumentException("Nombre y marca son obligatorios");

        // abrir conexión (try-with-resources para cerrar automáticamente)
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS)) {
            // empezamos transacción, persistir etiqueta y producto juntos, poner a flase el autocommit por si se hace sin que queramos
            con.setAutoCommit(false);
            try {
                // si la etiqueta existe pero no tiene id, insertarla y
                // recoger id de la etiqueta generada
                Etiqueta etiqueta = producto.getEtiqueta();
                Long etiquetaId = null;
                //si obtiene etiqueta se comprueba si tiene id, si el id
                // es 0 se ejecuta la sentencia, si no es 0 se guarda el
                // id (necesitado más tarde para la insercción del producto)
                if (etiqueta != null) {
                    if (etiqueta.getId() == 0) {
                        try (PreparedStatement pstmt = con.prepareStatement(sentenciaEtiqueta, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, etiqueta.getNombre());
                            //si no está la fecha de creación guarda la fecha actual
                            pstmt.setDate(2, Date.valueOf(etiqueta.getFechaCreacion() != null ? etiqueta.getFechaCreacion() : LocalDate.now()));
                            // ejecutamos la consulta de insert
                            pstmt.executeUpdate();
                            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    //id generado por la base de datos
                                    etiquetaId = rs.getLong(1);
                                    //asignamos al objeto
                                    etiqueta.setId(etiquetaId);
                                }
                            }
                        }
                        //si ya tiene id distinta de 0 la uso directamente
                    } else {
                        etiquetaId = etiqueta.getId();
                    }
                }

                // insertar producto (ya que ya tenemos la id de etiqueta)
                try (PreparedStatement pstmt = con.prepareStatement(sentenciaProducto, Statement.RETURN_GENERATED_KEYS)) {
                    // columna tipo para hacerlo Camisa, Pantalon o Chaqueta
                    pstmt.setString(1, producto.getClass().getSimpleName());
                    pstmt.setString(2, producto.getNombre());
                    pstmt.setString(3, producto.getMarca());
                    pstmt.setDouble(4, producto.getPrecioInicial());
                    // enumeraciones, guardamos su name()
                    pstmt.setString(5, producto.getTalla() != null ? producto.getTalla().name() : null);
                    pstmt.setString(6, producto.getColor() != null ? producto.getColor().name() : null);

                    // si existe etiqueta (ha salido bien la primera sentencia) le ponemos el id, si no se ponemos null
                    if (etiquetaId != null) pstmt.setLong(7, etiquetaId);
                    else pstmt.setNull(7, Types.BIGINT);

                    // descuento, si no existe ponemos null
                    if (producto.getDescuento() == null) {
                        pstmt.setNull(8, Types.VARCHAR);
                        pstmt.setNull(9, Types.DOUBLE);
                        // si existe obtenemos el tipo y el valor
                    } else {
                        String tipoDescuento = mapearTipoDescuentoPersistencia(producto.getDescuento());
                        Double valor = obtenerValorDescuento(producto.getDescuento());
                        // si no devuelve null se guardan los datos
                        if (tipoDescuento != null && valor != null) {
                            pstmt.setString(8, tipoDescuento);
                            pstmt.setDouble(9, valor);
                            // si algo es null (no se ha podido mapear y obtener valor ponemos null
                        } else {
                            pstmt.setNull(8, Types.VARCHAR);
                            pstmt.setNull(9, Types.DOUBLE);
                        }
                    }

                    // campos específicos de subclases
                    // de CAMISA
                    switch (producto) {
                        case Camisa camisa -> {
                            pstmt.setObject(10, camisa.getBotones(), Types.INTEGER);
                            // poner lo demás que no corresponde con camisa a null
                            pstmt.setNull(11, Types.INTEGER);
                            pstmt.setNull(12, Types.BOOLEAN);
                            pstmt.setNull(13, Types.INTEGER);
                            // de PANTALON
                            // mismo procedimiento que camisa pero distintos campos
                        }
                        case Pantalon pantalon -> {
                            pstmt.setNull(10, Types.INTEGER);
                            pstmt.setObject(11, pantalon.getBotones(), Types.INTEGER);
                            pstmt.setNull(12, Types.BOOLEAN);
                            pstmt.setNull(13, Types.INTEGER);
                            // de CHAQUETA
                            // mismo procedimiento que camisa pero distintos campos
                        }
                        case Chaqueta chaqueta -> {
                            pstmt.setNull(10, Types.INTEGER);
                            pstmt.setNull(11, Types.INTEGER);
                            pstmt.setObject(12, chaqueta.isConCapucha(), Types.BOOLEAN);
                            pstmt.setObject(13, chaqueta.getNivelAbrigo(), Types.INTEGER);
                            // si por cualquier cosa no pilla el tipo que es ponemos a null los valores
                        }
                        default -> {
                            pstmt.setNull(10, Types.INTEGER);
                            pstmt.setNull(11, Types.INTEGER);
                            pstmt.setNull(12, Types.BOOLEAN);
                            pstmt.setNull(13, Types.INTEGER);
                        }
                    }

                    //ejecutamos la consulta de insert del producto
                    pstmt.executeUpdate();
                    //obtenemos la clave generada por la base de datos y se la asignamos al objeto
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            producto.setId(rs.getLong(1));
                        }
                    }
                }

                // si tod ha ido bien hacemos commit de la transacción (se guardan correctamente los objetos)
                con.commit();
                // devolvemos el producto
                return producto;
                // de normal no se hace catch pero aquí hay que hacerlo para indicar que se
                // haga el rollback (que tod vuelva a como estaba antes de empezar la transacción si algo no ha ido bien
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
                //volver a poner el autocommit a true
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // determina que string persistir en descuento_tipo (guardar en la base de datos)
    private String mapearTipoDescuentoPersistencia(Descuento descuento) {
        return switch (descuento) {
            case null -> null;
            case DescuentoPorcentaje descuentoPorcentaje -> "DescuentoPorcentaje";
            case DescuentoFijo descuentoFijo -> "DescuentoFijo";
            default ->
                // usar clase simple name
                    descuento.getClass().getSimpleName();
        };
    }

    // extraer el número del valor del descuento ya sea fijo o porcentaje
    private Double obtenerValorDescuento(Descuento descuento) {
        // si no pilla descuento null
        return switch (descuento) {
            case null -> null;

            // si pilla un DescuentoPorcentaje devuelve un double del get de descuentoPorcentaje
            case DescuentoPorcentaje descuentoPorcentaje -> (double) descuentoPorcentaje.getDescuentoPorcentaje();


            // lo mismo pero con descuentoFijo
            case DescuentoFijo descuentoFijo -> (double) descuentoFijo.getDescuentoFijo();
            default ->

                // cualquier otra cosa devolver null
                    null;
        };

    }

    @Override
    public Optional<Producto> findById(long id) throws SQLException {
        String sentencia =
                "SELECT p.*, e.nombre AS etiqueta_nombre, e.fecha_creacion AS etiqueta_fecha_creacion " +
                        "FROM producto p LEFT JOIN etiqueta e ON p.etiqueta_id = e.id WHERE p.id = ?";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sentencia)) {

            // asignamos el id de la base de datos como el del ibjeto a buscar
            pstmt.setLong(1, id);
            // ejecutamos la sentencia
            try (ResultSet rs = pstmt.executeQuery()) {
                // si devuelve una fila
                if (rs.next()) {
                    // mapeamos el producto (se mapea tanto etiqueta como producto)
                    return Optional.of(mapearProductoCompleto(rs)); // mapeamos y devolvemos
                    // si no obtiene nada devolvemos un Optional vacío
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    // mapea un resultSet a un Producto Java (completo), construyendo una Camisa, un Pantalon o una Chaqueta, junto a su etiqueta y su descuento
    private Producto mapearProductoCompleto(ResultSet rs) throws SQLException {
        // leemos columna tipo para saber qué subclase instanciar
        String tipo = rs.getString("tipo");
        Producto producto;
        // primero los campos no comunes
        // si es Camisa, creamos la camisa y le asignamos botones
        switch (tipo) {
            // si es camisa, lo creamos y le asignamos los campos que corresponden con el tipo
            case "Camisa" -> {
                Camisa camisa = new Camisa();
                camisa.setBotones(rs.getInt("botones"));
                // guardamos el correspondiente producto en producto para poder seguir editanto el objeto solo cambiando la variable "producto"
                producto = camisa;
            }
            // si es pantalon igual
            case "Pantalon" -> {
                Pantalon pantalon = new Pantalon();
                pantalon.setBotones(rs.getInt("botones"));
                producto = pantalon;
            }
            // si es chaqueta igual
            case "Chaqueta" -> {
                Chaqueta chaqueta = new Chaqueta();
                chaqueta.setConCapucha(rs.getBoolean("con_capucha"));
                chaqueta.setNivelAbrigo(rs.getInt("nivel_abrigo"));
                producto = chaqueta;
            }
            default -> throw new SQLException("Tipo de producto desconocido: " + tipo);
        }

        // ahora los campos comunes
        producto.setId(rs.getLong("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setMarca(rs.getString("marca"));
        producto.setPrecioInicial(rs.getDouble("precio_inicial"));
        // mete la talla que leer d ela columna talla pero devuelve valores de las clases que son enum, es decir talla y color
        producto.setTalla(Talla.valueOf(rs.getString("talla")));
        producto.setColor(Color.valueOf(rs.getString("color")));

        // etiqueta, si la consulta devuelve columnas de etiqueta, las usamos para construir la Etiqueta (objeto)
        String nombreEtiqueta = rs.getString("etiqueta_nombre");
        // si es distinto de null (si no existe no se hace nada
        if (nombreEtiqueta != null) {
            // creamos etiqueta
            Etiqueta etiqueta = new Etiqueta();
            //  obtenemos la id de la etiqueta de la base de datos
            long etiquetaId = rs.getLong("etiqueta_id");
            // le ponemos el id a la etiqueta objeto
            etiqueta.setId(etiquetaId);
            etiqueta.setNombre(nombreEtiqueta);
            Date fecha = rs.getDate("etiqueta_fecha_creacion");
            if (fecha != null) etiqueta.setFechaCreacion(fecha.toLocalDate());
            producto.setEtiqueta(etiqueta);
        }

        // leemos tipo de descuento
        String tipoDescuento = rs.getString("descuento_tipo");
        // si no es null
        if (tipoDescuento != null) {
            // obtenemos valor del descuento
            double valor = rs.getDouble("descuento_valor");
            // creamos un objeto descuento mapeandolo para poder diferenciar entre porcentaje y fijo
            Descuento descuento = mapearDescuentoDesdeBD(tipoDescuento, valor);
            // le añadimos el descuento a el producto que estamos editando (mapeando realmente)
            producto.setDescuento(descuento);
            // si es null el descuento le asignamos null
        } else {
            producto.setDescuento(null);
        }

        // devolvemos el producto
        return producto;
    }

    // metodo público para que sea usado en otras clases
    public Producto mapearProductoPublic(ResultSet rs) throws SQLException {
        return mapearProductoCompleto(rs);
    }

    // crea la instancia concreta de descuento según el tipo y valor de la base de datos
    private Descuento mapearDescuentoDesdeBD(String tipo, double valor) {
        //si no tiene tipo devuelve null
        if (tipo == null) return null;

        // depende del tipo que detecte de la base de datos crea un objeto decuento porcentaje o fijo, con su valor
        return switch (tipo) {
            case "PORCENTAJE" -> new DescuentoPorcentaje((float) valor);
            case "FIJO" -> new DescuentoFijo((float) valor);
            default -> null;
        };
    }


    //metodo que devuelve todos los productos
    @Override
    public List<Producto> findAll() throws SQLException {
        String sentencia =
                "SELECT p.*, e.nombre AS etiqueta_nombre, e.fecha_creacion AS etiqueta_fecha_creacion " +
                        "FROM producto p LEFT JOIN etiqueta e ON p.etiqueta_id = e.id";
        // creamos la lista a devolver
        List<Producto> lista = new ArrayList<>();
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sentencia);
             // ejecutamos la consulta
             ResultSet rs = pstmt.executeQuery()) {
            // iteramos filas y mapeamos cada producto completo
            while (rs.next()) {
                lista.add(mapearProductoCompleto(rs));
            }
        }

        // devolvemos la lista
        return lista;
    }

    //metodo para actualizar un producto
    @Override
    public void update(Producto producto) throws SQLException {
        String sentenciaEtiqueta =
                "INSERT INTO etiqueta (nombre, fecha_creacion) VALUES (?, ?)";
        String sentenciaProducto =
                "UPDATE producto SET tipo = ?, nombre = ?, marca = ?, precio_inicial = ?, talla = ?, color = ?, " +
                        "etiqueta_id = ?, descuento_tipo = ?, descuento_valor = ?, botones = ?, bolsillos = ?, " +
                        "con_capucha = ?, nivel_abrigo = ? WHERE id = ?";
        // si el producto es nulo no se puede actualizar
        if (producto == null) throw new IllegalArgumentException("Producto nulo");
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS)) {
            // iniciamos transacción (autocommit false)
            con.setAutoCommit(false);
            // gestionamos la etiqueta, si el producto tiene etiqueta y es nueva (id=0)
            try {
                // obtener el objeto etiqueta asociado al producto
                Etiqueta et = producto.getEtiqueta();
                // variable que guardará el id final que pondremos en producto
                Long etiquetaId = null;
                // si el producto tiene etiqueta
                if (et != null) {
                    // si la etiqueta mo tiene id (0) la consideramos nueva y la insertamos en la base de datos
                    if (et.getId() == 0) {
                        try (PreparedStatement pstmt = con.prepareStatement(sentenciaEtiqueta, Statement.RETURN_GENERATED_KEYS)) {
                            // asignamos parámetros del insert (nombre y fecha de creación)
                            pstmt.setString(1, et.getNombre());
                            pstmt.setDate(2, Date.valueOf(et.getFechaCreacion() != null ? et.getFechaCreacion() : LocalDate.now()));
                            // ejecutamos
                            pstmt.executeUpdate();
                            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    // id generado por la base de datos
                                    etiquetaId = rs.getLong(1);
                                    // actualizamos el objeto etiqueta con su id
                                    et.setId(etiquetaId);
                                }
                            }
                        }
                        //si la etiqueta ya tiene id, la usamos tal cual (no la insertamos)
                    } else {
                        etiquetaId = et.getId();
                    }
                }

                // update del producto con todos sus campos
                try (PreparedStatement pstmt = con.prepareStatement(sentenciaProducto)) {
                    pstmt.setString(1, producto.getClass().getSimpleName());
                    pstmt.setString(2, producto.getNombre());
                    pstmt.setString(3, producto.getMarca());
                    pstmt.setDouble(4, producto.getPrecioInicial());
                    pstmt.setString(5, producto.getTalla() != null ? producto.getTalla().name() : null);
                    pstmt.setString(6, producto.getColor() != null ? producto.getColor().name() : null);

                    // etiqueta, igual lógica que en save
                    if (etiquetaId != null) pstmt.setLong(7, etiquetaId);
                    else pstmt.setNull(7, Types.BIGINT);

                    // descuento, igual lógica que en save
                    if (producto.getDescuento() == null) {
                        pstmt.setNull(8, Types.VARCHAR);
                        pstmt.setNull(9, Types.DOUBLE);
                    } else {
                        String tipoDescuento = mapearTipoDescuentoPersistencia(producto.getDescuento());
                        Double valor = obtenerValorDescuento(producto.getDescuento());
                        if (tipoDescuento != null && valor != null) {
                            pstmt.setString(8, tipoDescuento);
                            pstmt.setDouble(9, valor);
                        } else {
                            pstmt.setNull(8, Types.VARCHAR);
                            pstmt.setNull(9, Types.DOUBLE);
                        }
                    }

                    // campos específicos por subclase (misma idea que en save)
                    switch (producto) {
                        case Camisa camisa -> {
                            pstmt.setObject(10, camisa.getBotones(), Types.INTEGER);
                            pstmt.setNull(11, Types.INTEGER);
                            pstmt.setNull(12, Types.BOOLEAN);
                            pstmt.setNull(13, Types.INTEGER);
                        }
                        case Pantalon pantalon -> {
                            pstmt.setNull(10, Types.INTEGER);
                            pstmt.setObject(11, pantalon.getBotones(), Types.INTEGER);
                            pstmt.setNull(12, Types.BOOLEAN);
                            pstmt.setNull(13, Types.INTEGER);
                        }
                        case Chaqueta chaqueta -> {
                            pstmt.setNull(10, Types.INTEGER);
                            pstmt.setNull(11, Types.INTEGER);
                            pstmt.setObject(12, chaqueta.isConCapucha(), Types.BOOLEAN);
                            pstmt.setObject(13, chaqueta.getNivelAbrigo(), Types.INTEGER);
                        }
                        default -> {
                            pstmt.setNull(10, Types.INTEGER);
                            pstmt.setNull(11, Types.INTEGER);
                            pstmt.setNull(12, Types.BOOLEAN);
                            pstmt.setNull(13, Types.INTEGER);
                        }
                    }

                    // id al final (WHERE id = ?)
                    pstmt.setLong(14, producto.getId());
                    // ejecutar consulta
                    pstmt.executeUpdate();
                }
                // si tod va bien hacer commit (igual que save)
                con.commit();
                // si algo sale mal revertir
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
                // autocommit a true
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // metodo para borrar un producto por id
    @Override
    public void delete(long id) throws SQLException {
        String sentencia = "DELETE FROM producto WHERE id = ?";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            // para saber que producto es (por su id)
            pstmt.setLong(1, id);
            // ejecutar consulta
            pstmt.executeUpdate();
        }
    }

    // METODOS ESPECÍFICOS
    // metodo que devuelve una lista de los usuarios que tienen un producto concreto (por id) en favoritos
    @Override
    public List<Usuario> findUsuariosFavoritos(long productoId) throws SQLException {
        String sentencia =
                "SELECT u.* FROM usuario u JOIN usuario_producto_favorito upf ON u.id = upf.usuario_id WHERE upf.producto_id = ?";
        // creamos la lista de usuarios a devolver
        List<Usuario> usuarios = new ArrayList<>();
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            // para saber que producto concreto es
            pstmt.setLong(1, productoId);
            // ejecutamos la consulta
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // añadir los usuarios que haya obtenido mapeandolos
                    usuarios.add(JdbcUsuarioDAO.getInstance().mapearUsuarioPublic(rs));
                }
            }
        }

        // devolvemos la lista
        return usuarios;
    }

    // metodo para agregar un producto concreto a favoritos de un usuario concreto
    @Override
    public void agregarFavorito(long productoId, long usuarioId) throws SQLException {
        String sentencia =
                "INSERT INTO usuario_producto_favorito (usuario_id, producto_id) VALUES (?, ?)";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            // para saber el usuario que añade el producto y el producto concreto
            pstmt.setLong(1, usuarioId);
            pstmt.setLong(2, productoId);
            // ejecutar consulta
            pstmt.executeUpdate();
        }
    }

    // metodo para eliminar un producto concreto de favoritos de un usuario concreto
    @Override
    public void eliminarFavorito(long productoId, long usuarioId) throws SQLException {
        String sentencia =
                "DELETE FROM usuario_producto_favorito WHERE usuario_id = ? AND producto_id = ?";
        // igual que el eliminar favoritos pero con otra sentencia
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            pstmt.setLong(1, usuarioId);
            pstmt.setLong(2, productoId);
            pstmt.executeUpdate();
        }
    }
}
