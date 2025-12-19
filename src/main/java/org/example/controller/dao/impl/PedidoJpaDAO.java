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

    /*public void reset() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }*/

    public void reset() {
        if (emf != null && emf.isOpen()) emf.close();
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM linea_pedido").executeUpdate();
        em.createNativeQuery("DELETE FROM pedido").executeUpdate();
        em.createNativeQuery("DELETE FROM usuario_producto_favorito").executeUpdate();
        em.createNativeQuery("DELETE FROM producto").executeUpdate();
        em.createNativeQuery("DELETE FROM etiqueta").executeUpdate();
        em.createNativeQuery("DELETE FROM usuario").executeUpdate();
        em.getTransaction().commit();
        em.close();
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
    /*@Override
    public List<Pedido> findAll() throws SQLException {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Pedido p JOIN FETCH p.usuario",
                    Pedido.class
            ).getResultList();
        } finally {
            em.close();
        }
    }*/

    @Override
    public List<Pedido> findAll() throws SQLException {
        EntityManager em = emf.createEntityManager();
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p", Pedido.class).getResultList();

        // Esto asegura que Hibernate termine de leer el ResultSet antes de cerrar
        for(Pedido p : lista) {
            p.getLineasPedido().size();
        }

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
        EntityManager em = emf.createEntityManager();
        try {
            if (em.find(Usuario.class, usuarioId) == null)
                throw new SQLException("Cliente no existe");

            // Traemos los pedidos
            List<Pedido> pedidos = em.createQuery(
                            "SELECT p FROM Pedido p WHERE p.usuario.id = :uid", Pedido.class)
                    .setParameter("uid", usuarioId)
                    .getResultList();

            // TRUCO PARA EL ENUNCIADO: Forzamos la carga antes de cerrar el EM
            for (Pedido p : pedidos) {
                p.getLineasPedido().size(); // Acceso táctico para hidratar la lista Eager
            }

            return pedidos;
        } finally {
            em.close(); // Cerramos después de haber "tocado" todas las listas
        }
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
        Pedido p = findPedidoAux(em, usuarioId);
        p.setEstado(EstadoPedido.FINALIZADO);
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }


    // metodo auxiliar necesario para otros metodos de las funcionalidades
    /*private Pedido findPedidoPendiente(EntityManager em, long usuarioId) throws SQLException {
        // Quitamos los FETCH para evitar el error "AssertionFailure"
        List<Pedido> pedidos = em.createQuery(
                        "SELECT p FROM Pedido p " +
                                "WHERE p.usuario.id = :uid AND p.estado = :estado",
                        Pedido.class
                )
                .setParameter("uid", usuarioId)
                .setParameter("estado", EstadoPedido.PENDIENTE)
                .getResultList();

        if (pedidos.isEmpty()) {
            // Importante cerrar o hacer rollback si falla antes de lanzar la excepción
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
            throw new SQLException("No hay pedido pendiente para el usuario con id " + usuarioId);
        }

        return pedidos.get(0);
    }*/

    private Pedido findPedidoAux(EntityManager em, long uid) throws SQLException {
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
        Pedido p = findPedidoAux(em, usuarioId);
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
