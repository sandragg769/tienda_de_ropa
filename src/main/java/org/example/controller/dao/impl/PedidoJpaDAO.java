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
        // 1. Validar para el test 'crearPedidoIncorrecto'
        if (pedido.getUsuario() == null) {
            throw new SQLException("El pedido debe tener un usuario");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        // Persistimos el pedido (las líneas se guardan por CascadeType.ALL)
        em.persist(pedido);
        em.getTransaction().commit();
        em.close();
    }

    // buscar pedido y lineas de pedido
    @Override
    public Optional<Pedido> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT p FROM Pedido p " +
                                    "LEFT JOIN FETCH p.lineasPedido " +
                                    "JOIN FETCH p.usuario " +
                                    "WHERE p.id = :id", Pedido.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    // obtener todos los pedidos
    @Override
    public List<Pedido> findAll() throws SQLException {
        EntityManager em = emf.createEntityManager();
        try {
            // Usamos JOIN FETCH para cargar el usuario y las líneas en la misma consulta.
            // Esto evita el error de ResultSet closed y el problema de N+1.
            return em.createQuery(
                            "SELECT DISTINCT p FROM Pedido p " +
                                    "LEFT JOIN FETCH p.usuario " +
                                    "LEFT JOIN FETCH p.lineasPedido", Pedido.class)
                    .getResultList();
        } finally {
            // Es vital cerrar el EntityManager aquí
            em.close();
        }
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
        EntityManager em = emf.createEntityManager();
        // 4. Validar para 'pedidosPorClienteIncorrecto'
        Usuario u = em.find(Usuario.class, usuarioId);
        if (u == null) {
            em.close();
            throw new SQLException("Usuario no existe");
        }

        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p WHERE p.usuario.id = :uid", Pedido.class)
                .setParameter("uid", usuarioId)
                .getResultList();
        em.close();
        return lista;
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
    public List<LineaPedido> findLineasByPedido(long pedidoId) throws SQLException {
        EntityManager em = emf.createEntityManager();
        // 5. Validar para 'obtenerLineasDePedidoIncorrecto'
        Pedido p = em.find(Pedido.class, pedidoId);
        if (p == null) {
            em.close();
            throw new SQLException("Pedido no existe");
        }

        // Al ser EAGER o acceder dentro del contexto, devolvemos la lista
        List<LineaPedido> lineas = new ArrayList<>(p.getLineasPedido());
        em.close();
        return lineas;
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
    public void finalizarPedidoPendiente(long usuarioId, String metodoPago) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // 1. Usamos getResultList para evitar la excepción automática de JPA
        List<Pedido> resultados = em.createQuery(
                        "SELECT p FROM Pedido p WHERE p.usuario.id = :uid AND p.estado = :estado", Pedido.class)
                .setParameter("uid", usuarioId)
                .setParameter("estado", EstadoPedido.PENDIENTE)
                .getResultList();

        // 2. Si la lista está vacía, lanzamos nosotros la SQLException que el test espera
        if (resultados.isEmpty()) {
            em.close();
            throw new SQLException("No hay pedido pendiente para el usuario con id " + usuarioId);
        }

        // 3. Si hay resultados, sacamos el primero y actualizamos
        Pedido p = resultados.get(0);

        em.getTransaction().begin();
        p.setEstado(EstadoPedido.FINALIZADO);
        // Aquí podrías procesar el metodoPago si tuvieras un campo para ello
        em.merge(p);
        em.getTransaction().commit();

        em.close();
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
    public void cancelarPedidoPendiente(long usuarioId) throws SQLException {
        Pedido p = findPedidoPendiente(usuarioId);
        p.setEstado(EstadoPedido.CANCELADO);
        update(p);
    }

    // entregar un pedido
    @Override
    public void entregarPedido(long pedidoId) throws SQLException {
        Pedido p = findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));
        p.setEstado(EstadoPedido.ENTREGADO);
        update(p);
    }

}
