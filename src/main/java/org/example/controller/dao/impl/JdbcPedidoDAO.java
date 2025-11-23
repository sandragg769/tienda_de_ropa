package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.PedidoDAO;
import org.example.model.Usuario;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Producto;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class JdbcPedidoDAO implements PedidoDAO {
    private static JdbcPedidoDAO instance;

    private JdbcPedidoDAO() {}

    public static JdbcPedidoDAO getInstance() {
        if (instance == null) instance = new JdbcPedidoDAO();
        return instance;
    }

    @Override
    public void save(Pedido pedido) throws SQLException {

        String sqlPedido =
                "INSERT INTO pedido (fecha, estado, usuario_id) VALUES (?, ?, ?)";

        String sqlLinea =
                "INSERT INTO linea_pedido (cantidad, producto_id, pedido_id) VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS)) {

            con.setAutoCommit(false);

            try {
                // -----------------------
                // Insertar pedido
                // -----------------------
                long idPedido;

                try (PreparedStatement pstmt = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
                    pstmt.setString(2, pedido.getEstado().name());
                    pstmt.setLong(3, pedido.getUsuario().getId());
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        rs.next();
                        idPedido = rs.getLong(1);
                        pedido.setId(idPedido);
                    }
                }

                // -----------------------
                // Insertar líneas
                // -----------------------
                for (LineaPedido lp : pedido.getLineasPedido()) {

                    try (PreparedStatement pstmt = con.prepareStatement(sqlLinea, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt.setInt(1, lp.getCantidad());
                        pstmt.setLong(2, lp.getProducto().getId());
                        pstmt.setLong(3, idPedido);

                        pstmt.executeUpdate();

                        try (ResultSet rs = pstmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                lp.setId(rs.getLong(1));
                            }
                        }
                    }
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    @Override
    public Optional<Pedido> findById(long id) throws SQLException {

        String sql =
                "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " +
                        "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " +
                        "WHERE p.id = ?";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) return Optional.empty();

            Pedido pedido = mapearPedido(rs);
            pedido.setLineasPedido(new HashSet<>(findLineasByPedido(id)));

            return Optional.of(pedido);
        }
    }

    @Override
    public List<Pedido> findAll() throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql =
                "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " +
                        "FROM pedido p JOIN usuario u ON p.usuario_id = u.id";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Pedido pedido = mapearPedido(rs);
                pedido.setLineasPedido(new HashSet<>(findLineasByPedido(pedido.getId())));
                pedidos.add(pedido);
            }
        }

        return pedidos;
    }


    @Override
    public void update(Pedido pedido) throws SQLException {

        String sql =
                "UPDATE pedido SET fecha = ?, estado = ?, usuario_id = ? WHERE id = ?";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
            pstmt.setString(2, pedido.getEstado().name());
            pstmt.setLong(3, pedido.getUsuario().getId());
            pstmt.setLong(4, pedido.getId());

            pstmt.executeUpdate();
        }
    }


    @Override
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM pedido WHERE id = ?";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    // =========================================================
    // CONSULTAS ESPECÍFICAS
    // =========================================================

    @Override
    public List<Pedido> findByCliente(long usuarioId) throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql =
                "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " +
                        "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " +
                        "WHERE usuario_id = ?";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Pedido pedido = mapearPedido(rs);
                pedido.setLineasPedido(new HashSet<>(findLineasByPedido(pedido.getId())));
                pedidos.add(pedido);
            }
        }

        return pedidos;
    }

    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql =
                "SELECT p.*, u.id AS u_id, u.nombre AS u_nombre, u.email AS u_email " +
                        "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " +
                        "WHERE estado = ?";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, estado.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Pedido pedido = mapearPedido(rs);
                pedido.setLineasPedido(new HashSet<>(findLineasByPedido(pedido.getId())));
                pedidos.add(pedido);
            }
        }

        return pedidos;
    }

    @Override
    public List<LineaPedido> findLineasByPedido(long pedidoId) throws SQLException {

        List<LineaPedido> lineas = new ArrayList<>();

        String sql = "SELECT * FROM linea_pedido WHERE pedido_id = ?";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setLong(1, pedidoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lineas.add(mapearLinea(rs));
            }
        }

        return lineas;
    }

    @Override
    public void addLineaPedido(LineaPedido linea) throws SQLException {

        String sql =
                "INSERT INTO linea_pedido (cantidad, producto_id, pedido_id) VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS);
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, linea.getCantidad());
            pstmt.setLong(2, linea.getProducto().getId());
            pstmt.setLong(3, linea.getPedido().getId());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) linea.setId(rs.getLong(1));
            }
        }
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                "dni",                               // No se selecciona, colocar dummy
                null,
                LocalDate.now(),                     // dummy
                null,
                rs.getString("u_email"),
                "pass"
        );
        usuario.setId(rs.getLong("u_id"));
        usuario.setNombre(rs.getString("u_nombre"));

        Pedido pedido = new Pedido(usuario);
        pedido.setId(rs.getLong("id"));
        pedido.setFecha(rs.getDate("fecha").toLocalDate());
        pedido.setEstado(EstadoPedido.valueOf(rs.getString("estado")));

        return pedido;
    }

    private LineaPedido mapearLinea(ResultSet rs) throws SQLException {

        long productoId = rs.getLong("producto_id");

        // reutilizamos ProductoDAO
        Producto producto =
                JdbcProductoDAO.getInstance().findById(productoId).orElse(null);

        // pedido no lo seteamos para evitar referencias circulares
        LineaPedido lp = new LineaPedido(rs.getInt("cantidad"), producto, null);
        lp.setId(rs.getLong("id"));

        return lp;
    }

}
