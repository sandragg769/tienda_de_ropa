package org.example.controller.dao.interfaces;

import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PedidoDAO {
    void save(Pedido pedido) throws SQLException;

    Optional<Pedido> findById(long id) throws SQLException;

    List<Pedido> findAll() throws SQLException;

    void update(Pedido pedido) throws SQLException;

    void delete(long id) throws SQLException;

    // Operaciones específicas
    List<Pedido> findByCliente(long usuarioId) throws SQLException;

    List<Pedido> findByEstado(EstadoPedido estado) throws SQLException;

    List<LineaPedido> findLineasByPedido(long pedidoId) throws SQLException;

    void addLineaPedido(LineaPedido linea) throws SQLException;

    // FUNCIONALIDADES
    Pedido finalizarPedidoPendiente(long usuarioId, String metodoPago) throws SQLException;

    Pedido cancelarPedidoPendiente(long usuarioId) throws SQLException;

    void entregarPedido(long pedidoId) throws SQLException;
}
