package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.PedidoDAO;
import org.example.model.Usuario;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Producto;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class JdbcPedidoDAO implements PedidoDAO {
    // solo habrá una instancia de dao
    private static JdbcPedidoDAO instance;

    // constructor privado
    private JdbcPedidoDAO() {
    }

    // metodo estático que devuelve una instancia de este dao, si no existe se crea, si existe
    // se devuelve la existencia
    public static JdbcPedidoDAO getInstance() {
        if (instance == null) instance = new JdbcPedidoDAO();
        return instance;
    }

    public static void resetForTests() {
        instance = null;
    }

    // CRUD
    // inserta pedidos y todas sus lineas
    @Override
    public void save(Pedido pedido) throws SQLException {
        String sentenciaPedido = "INSERT INTO pedido (fecha, estado, usuario_id) VALUES (?, ?, ?)";
        String sentenciaLinea = "INSERT INTO linea_pedido (cantidad, producto_id, pedido_id) VALUES (?, ?, ?)";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS)) {
            // desactivar autocommit
            con.setAutoCommit(false);
            try {
                long idPedido;
                // insertar pedido y ejecutar la consulta
                try (PreparedStatement pstmt = con.prepareStatement(sentenciaPedido, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
                    pstmt.setString(2, pedido.getEstado().name());
                    pstmt.setLong(3, pedido.getUsuario().getId());
                    pstmt.executeUpdate();
                    // obtener id generado por la base de datos
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        rs.next();
                        idPedido = rs.getLong(1);
                        pedido.setId(idPedido);
                    }
                }

                // insertar lineasPedido
                for (LineaPedido lp : pedido.getLineasPedido()) {
                    // insertar cada línea y ejecutar consulta
                    try (PreparedStatement pstmt = con.prepareStatement(sentenciaLinea, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setInt(1, lp.getCantidad());
                        pstmt.setLong(2, lp.getProducto().getId());
                        pstmt.setLong(3, idPedido);
                        pstmt.executeUpdate();
                        // guardar id generado en el objeto lineaPedidio
                        try (ResultSet rs = pstmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                lp.setId(rs.getLong(1));
                            }
                        }
                    }
                }

                // cerrar la transacción
                con.commit();
                // si no sale bien la transacción no se guarda nada
            } catch (SQLException e) {
                con.rollback();
                throw e;
                // poner autocommit a true
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // buscar pedido y lineas de pedido
    @Override
    public Optional<Pedido> findById(long id) throws SQLException {
        String sentencia = "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " + "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " + "WHERE p.id = ?";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            // ejecutar consulta
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            // si no existe devuelve empty
            if (!rs.next()) return Optional.empty();
            // pasa el resultSet a un objeto pedido (mapear)
            Pedido pedido = mapearPedido(rs);
            // carga todas las lineas del pedido (hashSet para evitar duplicados)
            pedido.setLineasPedido(new HashSet<>(findLineasByPedido(id)));

            // devolver el pedido
            return Optional.of(pedido);
        }
    }

    // metodo auxiliar usado varias veces para mapear pedidos
    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        // mapeamos usuario
        Usuario usuario = JdbcUsuarioDAO.getInstance().mapearUsuarioPublic(rs);

        // creamos el objeto pedido y lo rellenamos
        Pedido pedido = new Pedido(usuario);
        pedido.setId(rs.getLong("id"));
        pedido.setFecha(rs.getDate("fecha").toLocalDate());
        pedido.setEstado(EstadoPedido.valueOf(rs.getString("estado")));

        // devolver pedido
        return pedido;
    }

    // obtener todos los pedidos
    @Override
    public List<Pedido> findAll() throws SQLException {
        // lista a devolver
        List<Pedido> pedidos = new ArrayList<>();
        String sentencia = "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " + "FROM pedido p JOIN usuario u ON p.usuario_id = u.id";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sentencia)) {
            // mapear los rs obtenidos a objetos pedido
            while (rs.next()) {
                Pedido pedido = mapearPedido(rs);
                // añadir las lineasPedido al pedido
                pedido.setLineasPedido(new HashSet<>(findLineasByPedido(pedido.getId())));
                // añadir el pedido completo a la lista a devolver
                pedidos.add(pedido);
            }
        }

        // devolver lista de pedidos
        return pedidos;
    }

    // actualizar pedido (no las lineas de pedido, muy complejo)
    @Override
    public void update(Pedido pedido) throws SQLException {
        String sentencia = "UPDATE pedido SET fecha = ?, estado = ?, usuario_id = ? WHERE id = ?";
        // conexión
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            // ejecutar consulta
            pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
            pstmt.setString(2, pedido.getEstado().name());
            pstmt.setLong(3, pedido.getUsuario().getId());
            pstmt.setLong(4, pedido.getId());
            pstmt.executeUpdate();
        }
    }


    // metodo que borrar pedido por su id
    @Override
    public void delete(long id) throws SQLException {
        String setencia = "DELETE FROM pedido WHERE id = ?";
        // conexión y ejecutar la consulta
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(setencia)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }


    // METODOS ESPECÍFICOS
    // obtener pedidos por cliente (es decir todos los pedidos de un cliente)
    @Override
    public List<Pedido> findByCliente(long usuarioId) throws SQLException {
        // lista a devolver
        List<Pedido> pedidos = new ArrayList<>();
        String sentencia = "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " + "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " + "WHERE usuario_id = ?";
        // conexión y ejecutar consulta
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            //mapear lo que se obtiene de la consulta
            while (rs.next()) {
                Pedido pedido = mapearPedido(rs);
                pedido.setLineasPedido(new HashSet<>(findLineasByPedido(pedido.getId())));
                pedidos.add(pedido);
            }
        }

        // devolver lista
        return pedidos;
    }

    // encontrar pedidos por estado de este
    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sentencia = "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " + "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " + "WHERE estado = ?";
        // conexión y ejecución de la consulta
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            pstmt.setString(1, estado.name());
            ResultSet rs = pstmt.executeQuery();
            // mapear los pedidos obtenidos junto a sus lineas de pedido
            while (rs.next()) {
                Pedido pedido = mapearPedido(rs);
                pedido.setLineasPedido(new HashSet<>(findLineasByPedido(pedido.getId())));
                pedidos.add(pedido);
            }
        }

        // devolver lista
        return pedidos;
    }

    // metodo para obtener las lineas de pedido de un pedido concreto
    @Override
    public List<LineaPedido> findLineasByPedido(long pedidoId) throws SQLException {
        // lista a devolver
        List<LineaPedido> lineas = new ArrayList<>();
        String sentencia = "SELECT * FROM linea_pedido WHERE pedido_id = ?";
        // conexión y ejecutar consulta
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(sentencia)) {
            pstmt.setLong(1, pedidoId);
            ResultSet rs = pstmt.executeQuery();
            // añadir a la lista de lineas las lineas mapeadas
            while (rs.next()) {
                lineas.add(mapearLinea(rs));
            }
        }

        // devolver lista de lineas
        return lineas;
    }

    // metodo auxiliar para mapear lineas
    private LineaPedido mapearLinea(ResultSet rs) throws SQLException {
        // obtenemos el id de la base de datos del producto a añadir a la linea
        long productoId = rs.getLong("producto_id");

        // llama al dao de productos para obtener el producto completo
        Producto producto = JdbcProductoDAO.getInstance().findById(productoId).orElse(null);

        // crea una nueva instancia de lineaPedido a la que se le pasa cantidad, producto (mapeado antes), pedido null para evitar referencias circulares
        LineaPedido linea = new LineaPedido(rs.getInt("cantidad"), producto, null);
        // asignar al objeto el id real de la linea pedido (de la base de datos)
        linea.setId(rs.getLong("id"));

        // devolver la linea
        return linea;
    }

    // añadir linea de pedido
    @Override
    public void addLineaPedido(LineaPedido linea) throws SQLException {
        String sentencia = "INSERT INTO linea_pedido (cantidad, producto_id, pedido_id) VALUES (?, ?, ?)";
        // conexión y ejecutar consulta
        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS); PreparedStatement pstmt = con.prepareStatement(sentencia, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, linea.getCantidad());
            pstmt.setLong(2, linea.getProducto().getId());
            pstmt.setLong(3, linea.getPedido().getId());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) linea.setId(rs.getLong(1));
            }
        }
    }

}
