package org.example.controller.dao.impl;

import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.model.Usuario;
import org.example.model.descuento.Descuento;
import org.example.model.descuento.DescuentoFijo;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Chaqueta;
import org.example.model.producto.tipo_de_productos.Pantalon;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcProductoDAO implements ProductoDAO {
    private static volatile JdbcProductoDAO instance;

    private JdbcProductoDAO() {
    }

    public static JdbcProductoDAO getInstance() {
        if (instance == null) {
            synchronized (JdbcProductoDAO.class) {
                if (instance == null) instance = new JdbcProductoDAO();
            }
        }
        return instance;
    }

    @Override
    public Producto save(Producto producto) throws SQLException {
        String sentencia1 =
                "INSERT INTO etiqueta (nombre, fecha_creacion) VALUES (?, ?)";
        String sentencia2 =
                "INSERT INTO producto (tipo, nombre, marca, precio_inicial, talla, color, etiqueta_id, " +
                        "descuento_tipo, descuento_valor, botones, bolsillos, con_capucha, nivel_abrigo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Validaciones básicas
        if (producto == null) throw new IllegalArgumentException("Producto nulo");
        if (producto.getNombre() == null || producto.getMarca() == null)
            throw new IllegalArgumentException("Nombre y marca son obligatorios");

        try (Connection con = DriverManager.getConnection(DatabaseConf.URL, DatabaseConf.USER, DatabaseConf.PASS)) {
            // transacción: persistir etiqueta y producto juntos
            con.setAutoCommit(false);
            try {
                // 1) persistir etiqueta si existe y no tiene id
                Etiqueta et = producto.getEtiqueta();
                Long etiquetaId = null;
                if (et != null) {
                    if (et.getId() == 0) {
                        try (PreparedStatement pstmt = con.prepareStatement(sentencia1, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, et.getNombre());
                            pstmt.setDate(2, Date.valueOf(et.getFechaCreacion() != null ? et.getFechaCreacion() : LocalDate.now()));
                            pstmt.executeUpdate();
                            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                                if (rs.next()) {
                                    etiquetaId = rs.getLong(1);
                                    et.setId(etiquetaId);
                                }
                            }
                        }
                    } else {
                        etiquetaId = et.getId();
                    }
                }

                // 2) insertar producto
                try (PreparedStatement pstmt = con.prepareStatement(sentencia2, Statement.RETURN_GENERATED_KEYS)) {
                    // tipo = clase simple name (Camisa, Pantalon, Chaqueta)
                    pstmt.setString(1, producto.getClass().getSimpleName());
                    pstmt.setString(2, producto.getNombre());
                    pstmt.setString(3, producto.getMarca());
                    pstmt.setDouble(4, producto.getPrecioInicial());
                    pstmt.setString(5, producto.getTalla() != null ? producto.getTalla().name() : null);
                    pstmt.setString(6, producto.getColor() != null ? producto.getColor().name() : null);

                    if (etiquetaId != null) pstmt.setLong(7, etiquetaId);
                    else pstmt.setNull(7, Types.BIGINT);

                    // descuento: persistir tipo y valor según instancia (si existe)
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

                    // campos específicos de subclases
                    if (producto instanceof Camisa) {
                        Camisa c = (Camisa) producto;
                        pstmt.setObject(10, c.getBotones(), Types.INTEGER);
                        pstmt.setNull(11, Types.INTEGER);
                        pstmt.setNull(12, Types.BOOLEAN);
                        pstmt.setNull(13, Types.INTEGER);
                    } else if (producto instanceof Pantalon) {
                        Pantalon p = (Pantalon) producto;
                        pstmt.setNull(10, Types.INTEGER);
                        pstmt.setObject(11, p.getBotones(), Types.INTEGER);
                        pstmt.setNull(12, Types.BOOLEAN);
                        pstmt.setNull(13, Types.INTEGER);
                    } else if (producto instanceof Chaqueta) {
                        Chaqueta ch = (Chaqueta) producto;
                        pstmt.setNull(10, Types.INTEGER);
                        pstmt.setNull(11, Types.INTEGER);
                        pstmt.setObject(12, ch.isConCapucha(), Types.BOOLEAN);
                        pstmt.setObject(13, ch.getNivelAbrigo(), Types.INTEGER);
                    } else {
                        // por seguridad, dejar nulos
                        pstmt.setNull(10, Types.INTEGER);
                        pstmt.setNull(11, Types.INTEGER);
                        pstmt.setNull(12, Types.BOOLEAN);
                        pstmt.setNull(13, Types.INTEGER);
                    }

                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            producto.setId(rs.getLong(1));
                        }
                    }
                }

                con.commit();
                return producto;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Determina qué string persistir en descuento_tipo
    private String mapearTipoDescuentoPersistencia(Descuento d) {
        if (d == null) return null;
        if (d instanceof DescuentoPorcentaje) return "DescuentoPorcentaje";
        if (d instanceof DescuentoFijo) return "DescuentoFijo";
        // fallback: usar clase simple name
        return d.getClass().getSimpleName();
    }

    @Override
    public Optional<Producto> findById(long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Producto> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public void update(Producto producto) throws SQLException {

    }

    @Override
    public void delete(long id) throws SQLException {

    }

    @Override
    public List<Usuario> findUsuariosFavoritos(long productoId) throws SQLException {
        return List.of();
    }

    @Override
    public void agregarFavorito(long productoId, long usuarioId) throws SQLException {

    }

    @Override
    public void eliminarFavorito(long productoId, long usuarioId) throws SQLException {

    }
}
