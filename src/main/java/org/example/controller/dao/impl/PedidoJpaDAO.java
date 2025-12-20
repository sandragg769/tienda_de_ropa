package org.example.controller.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.controller.dao.interfaces.PedidoDAO;
import org.example.model.Usuario;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoJpaDAO implements PedidoDAO {
    // solo habrá una instancia de dao
    private static PedidoJpaDAO instance;

    //
    private EntityManagerFactory emf;

    private PedidoJpaDAO() {
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }

    public static PedidoJpaDAO getInstance() {
        if (instance == null) instance = new PedidoJpaDAO();
        return instance;
    }

    public void reset() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }


    // CRUD
    // inserta pedidos y todas sus lineas
    @Override
    public void save(Pedido pedido) throws SQLException {
        if (pedido.getUsuario() == null) throw new SQLException("Usuario null");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        // Usamos merge en lugar de persist si el usuario ya existe en la BD
        Pedido persistido = em.merge(pedido);
        pedido.setId(persistido.getId()); // Sincronizamos el ID para el test

        em.getTransaction().commit();
        em.close();
    }

    // buscar pedido y lineas de pedido
    @Override
    public Optional<Pedido> findById(long id) {
        EntityManager em = emf.createEntityManager();
        Pedido p = em.find(Pedido.class, id);
        if (p != null) {
            // Forzamos que los datos estén realmente en memoria
            p.getLineasPedido().size();
            p.getUsuario().getNombre();
        }
        em.close();
        return Optional.ofNullable(p);
    }

    // obtener todos los pedidos
    @Override
    public List<Pedido> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p", Pedido.class).getResultList();
        em.close();
        return lista;
    }

    // actualizar pedido (no las lineas de pedido, muy complejo)
    @Override
    public void update(Pedido pedido) throws SQLException {
        EntityManager em = emf.createEntityManager();
        // 2. Validar para 'actualizarPedidoIncorrecto'
        Pedido existente = em.find(Pedido.class, pedido.getId());
        if (existente == null) {
            em.close();
            throw new SQLException("Pedido no encontrado");
        }

        em.getTransaction().begin();
        em.merge(pedido);
        em.getTransaction().commit();
        em.close();
    }

    // metodo que borrar pedido por su id
    @Override
    public void delete(long id) throws SQLException {
        EntityManager em = emf.createEntityManager();
        // 3. Validar para 'eliminarPedidoIncorrecto'
        Pedido p = em.find(Pedido.class, id);
        if (p == null) {
            em.close();
            throw new SQLException("ID inexistente");
        }

        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();
        em.close();
    }


    // METODOS ESPECÍFICOS
    // obtener pedidos por cliente (es decir todos los pedidos de un cliente)
    @Override
    public List<Pedido> findByCliente(long usuarioId) throws SQLException {
        try (EntityManager em = emf.createEntityManager()) {
            if (em.find(Usuario.class, usuarioId) == null)
                throw new SQLException("Cliente no existe");

            // Traemos los pedidos
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.usuario.id = :uid", Pedido.class)
                    .setParameter("uid", usuarioId)
                    .getResultList();
        }
        // Cerramos después de haber "tocado" todas las listas
    }

    // encontrar pedidos por estado de este
    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) {
        if (estado == null) throw new NullPointerException();
        EntityManager em = emf.createEntityManager();
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p WHERE p.estado = :est", Pedido.class)
                .setParameter("est", estado)
                .getResultList();
        List<Pedido> segura = new ArrayList<>(lista);
        em.close();
        return segura;
    }

    // metodo para obtener las lineas de pedido de un pedido concreto
    @Override
    public List<LineaPedido> findLineasByPedido(long pedidoId) throws SQLException {
        EntityManager em = emf.createEntityManager();
        Pedido p = em.find(Pedido.class, pedidoId);
        if (p == null) {
            em.close();
            throw new SQLException("Pedido no existe");
        }
        List<LineaPedido> lineas = new ArrayList<>(p.getLineasPedido());
        em.close();
        return lineas;
    }

    // añadir linea de pedido
    @Override
    public void addLineaPedido(LineaPedido linea) throws SQLException {
        EntityManager em = emf.createEntityManager();
        if (linea.getPedido() == null || em.find(Pedido.class, linea.getPedido().getId()) == null) {
            em.close();
            throw new SQLException("Pedido no existente");
        }
        em.getTransaction().begin();
        em.persist(linea);
        em.getTransaction().commit();
        em.close();
    }


    // FUNCIONALIDADES
    // busca pedido pendiente de un usuario concreto y lo finaliza
    @Override
    public void finalizarPedidoPendiente(long usuarioId, String metodoPago) throws SQLException {
        EntityManager em = emf.createEntityManager();
        // Importante: em.clear() ayuda a evitar el AssertionFailure de Hibernate
        em.clear();
        em.getTransaction().begin();
        Pedido p = findPedidoPendiente(em, usuarioId);
        p.setEstado(EstadoPedido.FINALIZADO);
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }

    // metodo auxiliar necesario para otros metodos de las funcionalidades
    private Pedido findPedidoPendiente(EntityManager em, long uid) throws SQLException {
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p WHERE p.usuario.id = :uid AND p.estado = :est", Pedido.class)
                .setParameter("uid", uid)
                .setParameter("est", EstadoPedido.PENDIENTE)
                .getResultList();
        if (lista.isEmpty()) {
            throw new SQLException("No hay pedido pendiente para el usuario con id " + uid);
        }
        return lista.get(0);
    }

    // busca pedido pendiente de un usuario concreto y lo cancela
    @Override
    public void cancelarPedidoPendiente(long usuarioId) throws SQLException {
        EntityManager em = emf.createEntityManager();
        em.clear();
        em.getTransaction().begin();
        Pedido p = findPedidoPendiente(em, usuarioId);
        p.setEstado(EstadoPedido.CANCELADO);
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }

    // entregar un pedido
    @Override
    public void entregarPedido(long pedidoId) throws SQLException {
        EntityManager em = emf.createEntityManager();
        Pedido p = em.find(Pedido.class, pedidoId);
        if (p == null) {
            em.close();
            throw new SQLException("Pedido no existe");
        }
        em.getTransaction().begin();
        p.setEstado(EstadoPedido.ENTREGADO);
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }
}
