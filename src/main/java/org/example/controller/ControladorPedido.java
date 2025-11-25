package org.example.controller;

import org.example.controller.dao.impl.JdbcPedidoDAO;
import org.example.controller.dao.interfaces.PedidoDAO;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.utils.GestorFicherosGSON;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ControladorPedido {
    private final PedidoDAO pedidoDAO = JdbcPedidoDAO.getInstance();

    // CRUD
    public void crearPedido(Pedido pedido) throws SQLException {
        pedidoDAO.save(pedido);
    }

    public Optional<Pedido> buscarPorId(long id) throws SQLException {
        return pedidoDAO.findById(id);
    }

    public List<Pedido> obtenerTodos() throws SQLException {
        return pedidoDAO.findAll();
    }

    public void actualizarPedido(Pedido pedido) throws SQLException {
        pedidoDAO.update(pedido);
    }

    public void eliminarPedido(long id) throws SQLException {
        pedidoDAO.delete(id);
    }


    // METODOS ESPECÍFICOS
    public List<Pedido> pedidosPorCliente(long usuarioId) throws SQLException {
        return pedidoDAO.findByCliente(usuarioId);
    }

    public List<Pedido> pedidosPorEstado(EstadoPedido estado) throws SQLException {
        return pedidoDAO.findByEstado(estado);
    }

    public List<LineaPedido> lineasDePedido(long pedidoId) throws SQLException {
        return pedidoDAO.findLineasByPedido(pedidoId);
    }

    public void agregarLineaPedido(LineaPedido lineaPedido) throws SQLException {
        pedidoDAO.addLineaPedido(lineaPedido);
    }


    // GESTOR FICHEROS
    public void exportarPedidosGson() throws SQLException {
        List<Pedido> listaPedidos = pedidoDAO.findAll();
        GestorFicherosGSON.exportarPedidosAGson(listaPedidos, "pedidos.json");
    }

    public void importarPedidosGson() throws SQLException {
        List<Pedido> pedidosImportados = GestorFicherosGSON.importarPedidosDesdeGson("pedidos.json");
        // importar significa persistir en BD
        for (Pedido p : pedidosImportados) {
            pedidoDAO.save(p);
        }
    }

}
