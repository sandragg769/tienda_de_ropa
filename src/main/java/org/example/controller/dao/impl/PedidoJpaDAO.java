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

// misma estructura que implementación de UsuarioJpaDAO, los metodos no están tan explicados al ser muy parecidos todos
// hacer algunos con try-with-resources y otors manual para saber hacer los dos, lo mejor sería todos con try-with-resources
public class PedidoJpaDAO implements PedidoDAO {
    private static PedidoJpaDAO instance;

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
        // validar si existe cliente
        if (pedido.getUsuario() == null) throw new SQLException("Usuario null");

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // usamos merge en lugar de persist si el usuario ya existe en la BD (en estado DETACHED),
            // merge busca al usuario en la BD, lo conecta al pedido y guarda ambos, si se usara persist
            // JPA intentará insertar de nuevo el usuario
            Pedido persistido = em.merge(pedido);
            // sincronizamos el ID generado para que el objeto original lo tenga
            pedido.setId(persistido.getId());
            em.getTransaction().commit();
        }
    }

    // buscar pedido y lineas de pedido
    @Override
    public Optional<Pedido> findById(long id) {
        try (EntityManager em = emf.createEntityManager()) {
            // cargar pedido
            Pedido p = em.find(Pedido.class, id);
            // validar existencia
            if (p != null) {
                // obligar a JPA a hacer un select de las líneas ahora mientras la conexión sigue abierta
                p.getLineasPedido().size();
                p.getUsuario().getNombre();
            }
            return Optional.ofNullable(p);
        }
    }

    // obtener todos los pedidos
    @Override
    public List<Pedido> findAll() {
        EntityManager em = emf.createEntityManager();

        // consulta JPQL devuelve una lista de entidades en estado MANAGED
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p", Pedido.class).getResultList();
        // cierre manual, las entidades pasan a estado DETACHED
        em.close();
        return lista;
    }

    // actualizar pedido (no las lineas de pedido, muy complejo)
    @Override
    public void update(Pedido pedido) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // comprobar que existe el pedido, pasa a estado MANAGED si lo encuentra
        Pedido existente = em.find(Pedido.class, pedido.getId());
        // si no existe se sale del metodo cerrando el EM
        if (existente == null) {
            em.close();
            throw new SQLException("Pedido no encontrado");
        }

        em.getTransaction().begin();
        // merge convierte el objeto "pedido" (que está DETACHED) en un objeto MANAGED, se aplican los cambios al finalizar transacción
        em.merge(pedido);
        em.getTransaction().commit();
        em.close();
    }

    // metodo que borrar pedido por su id
    @Override
    public void delete(long id) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // buscar peddio en BD, si existe pasa a estado MANAGED
        Pedido p = em.find(Pedido.class, id);
        if (p == null) {
            em.close();
            throw new SQLException("ID inexistente");
        }

        em.getTransaction().begin();
        // marca el objeto para eliminar al terminar transacción
        em.remove(p);
        em.getTransaction().commit();
        em.close();
    }


    // METODOS ESPECÍFICOS
    // obtener pedidos por cliente (es decir todos los pedidos de un cliente)
    @Override
    public List<Pedido> findByCliente(long usuarioId) throws SQLException {
        try (EntityManager em = emf.createEntityManager()) {

            // validar existencia de usuario, si no existe lanza error
            if (em.find(Usuario.class, usuarioId) == null) throw new SQLException("Cliente no existe");

            // consulta JPQL devuelve lista de pedidos MANAGED
            return em.createQuery("SELECT p FROM Pedido p WHERE p.usuario.id = :uid", Pedido.class).setParameter("uid", usuarioId).getResultList();
        }
    }

    // encontrar pedidos por estado de este
    @Override
    public List<Pedido> findByEstado(EstadoPedido estado) {
        // comprueba que el estado del parámetro no es null
        if (estado == null) throw new NullPointerException();

        EntityManager em = emf.createEntityManager();

        // devuelve lista MANAGED
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p WHERE p.estado = :est", Pedido.class).setParameter("est", estado).getResultList();
        // copia la lista para separarla del EM
        List<Pedido> copia = new ArrayList<>(lista);
        em.close();
        return copia;
    }

    // metodo para obtener las lineas de pedido de un pedido concreto
    @Override
    public List<LineaPedido> findLineasByPedido(long pedidoId) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // cargar pedido (MANAGED)
        Pedido p = em.find(Pedido.class, pedidoId);
        if (p == null) {
            em.close();
            throw new SQLException("Pedido no existe");
        }

        // copia para evitar errores
        List<LineaPedido> lineas = new ArrayList<>(p.getLineasPedido());
        em.close();
        return lineas;
    }

    // añadir linea de pedido
    @Override
    public void addLineaPedido(LineaPedido linea) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // comprobamos que la línea tiene un pedido asignado y que el pedido existe en la BD
        if (linea.getPedido() == null || em.find(Pedido.class, linea.getPedido().getId()) == null) {
            em.close();
            throw new SQLException("Pedido no existente");
        }

        em.getTransaction().begin();
        // persist crea una entidad en estado MANAGED y lo inserta con el commit
        em.persist(linea);
        em.getTransaction().commit();
        em.close();
    }


    // FUNCIONALIDADES
    // busca pedido pendiente de un usuario concreto y lo finaliza
    @Override
    public void finalizarPedidoPendiente(long usuarioId, String metodoPago) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // evitar conflictos de versiones con Hibernate
        em.clear();

        em.getTransaction().begin();
        // obtenemos el pedido pendiente (MANAGED)
        Pedido p = findPedidoPendiente(em, usuarioId);
        // cambiamos estado
        p.setEstado(EstadoPedido.FINALIZADO);
        // merge para que JPA actualice el objeto en BD
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }

    // metodo auxiliar necesario para otros metodos de las funcionalidades
    private Pedido findPedidoPendiente(EntityManager em, long uid) throws SQLException {
        // obtener pedidos pendientes para el usuario
        List<Pedido> lista = em.createQuery("SELECT p FROM Pedido p WHERE p.usuario.id = :uid AND p.estado = :est", Pedido.class).setParameter("uid", uid).setParameter("est", EstadoPedido.PENDIENTE).getResultList();

        // si la lista es empty da exception
        if (lista.isEmpty()) {
            throw new SQLException("No hay pedido pendiente para el usuario con id " + uid);
        }

        // devolvemos el primero
        return lista.get(0);
    }

    // busca pedido pendiente de un usuario concreto y lo cancela
    @Override
    public void cancelarPedidoPendiente(long usuarioId) throws SQLException {
        // igual que metodo de finalizar
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
        // parecido al metodo de finalizar pero no se busca pedido pendiente, se busca el finalizado
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
