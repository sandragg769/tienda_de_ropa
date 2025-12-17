package org.example.controller.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.controller.dao.interfaces.PedidoDAO;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class PedidoJpaDAO implements PedidoDAO {
    // solo habrá una instancia de dao
    private static PedidoJpaDAO instance;

    //
    private final EntityManagerFactory emf;

    private PedidoJpaDAO() {
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }

    public static PedidoJpaDAO getInstance() {
        if (instance == null) instance = new PedidoJpaDAO();
        return instance;
    }

    // CRUD
    // inserta pedidos y todas sus lineas
    @Override
    public void save(Pedido pedido) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // cascada líneas
            em.persist(pedido);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // buscar pedido y lineas de pedido
    @Override
    public Optional<Pedido> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Pedido.class, id));
        } finally {
            em.close();
        }
    }

    // obtener todos los pedidos
    @Override
    public List<Pedido> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p", Pedido.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // actualizar pedido (no las lineas de pedido, muy complejo)
    @Override
    public void update(Pedido pedido) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(pedido);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }


    // metodo que borrar pedido por su id
    @Override
    public void delete(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Pedido p = em.find(Pedido.class, id);
            em.getTransaction().begin();
            em.remove(p);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }


    // METODOS ESPECÍFICOS
    // obtener pedidos por cliente (es decir todos los pedidos de un cliente)
    @Override
    public List<Pedido> findByCliente(long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.usuario.id = :id", Pedido.class)
                    .setParameter("id", usuarioId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // encontrar pedidos por estado de este
    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.estado = :estado", Pedido.class)
                    .setParameter("estado", estado)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // metodo para obtener las lineas de pedido de un pedido concreto
    @Override
    public List<LineaPedido> findLineasByPedido(long pedidoId) {
        EntityManager em = emf.createEntityManager();
        try {
            Pedido p = em.find(Pedido.class, pedidoId);
            return (List<LineaPedido>) p.getLineasPedido();
        } finally {
            em.close();
        }
    }


    // añadir linea de pedido
    @Override
    public void addLineaPedido(LineaPedido linea) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(linea);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }


    // FUNCIONALIDADES
    // busca pedido pendiente de un usuario concreto y lo finaliza
    @Override
    public Pedido finalizarPedidoPendiente(long usuarioId, String metodoPago) {
        Pedido p = findPedidoPendiente(usuarioId);
        p.setEstado(EstadoPedido.FINALIZADO);
        update(p);
        return p;
    }

    // metodo auxiliar necesario para otros metodos de las funcionalidades
    private Pedido findPedidoPendiente(long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.usuario.id = :id AND p.estado = :estado",
                            Pedido.class)
                    .setParameter("id", usuarioId)
                    .setParameter("estado", EstadoPedido.PENDIENTE)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    // busca pedido pendiente de un usuario concreto y lo cancela
    @Override
    public Pedido cancelarPedidoPendiente(long usuarioId) {
        Pedido p = findPedidoPendiente(usuarioId);
        p.setEstado(EstadoPedido.CANCELADO);
        update(p);
        return p;
    }

    // entregar un pedido
    @Override
    public void entregarPedido(long pedidoId) {
        Pedido p = findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));
        p.setEstado(EstadoPedido.ENTREGADO);
        update(p);
    }

}
