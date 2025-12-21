package org.example.controller.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.model.Usuario;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// misma estructura que implementación de UsuarioJpaDAO, los metodos no están tan explicados al ser muy parecidos todos
public class ProductoJpaDAO implements ProductoDAO {
    private static ProductoJpaDAO instance;

    private EntityManagerFactory emf;

    private ProductoJpaDAO() {
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }

    public static ProductoJpaDAO getInstance() {
        if (instance == null) instance = new ProductoJpaDAO();
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
    // metodo que guarda un producto en la base de datos
    @Override
    public Producto save(Producto producto) throws SQLException {
        // validamos datos válidos
        if (producto.getNombre() == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }

        try (EntityManager em = emf.createEntityManager()) {
            // validación de etiqueta (comprobamos que existe en la BD) si no existe lanzamos SQL Exception
            if (producto.getEtiqueta() != null && producto.getEtiqueta().getId() != 0) {
                Etiqueta e = em.find(Etiqueta.class, producto.getEtiqueta().getId());
                if (e == null) {
                    throw new SQLException("La etiqueta con ID " + producto.getEtiqueta().getId() + " no existe");
                }
            }

            em.getTransaction().begin();
            // el objeto producto es gestionado por JPA, se asigna ID, aquí el objeto ya es MANAGED
            em.persist(producto);
            // JPA genera y envía el comando para insertar a la base de datos
            em.getTransaction().commit();

            // se devuelve el producto
            return producto;
        }
    }

    // metodo para encontrar un producto en concreto por id
    @Override
    public Optional<Producto> findById(long id) {
        try (EntityManager em = emf.createEntityManager()) {
            // en lugar de un simple find, usar una Query para hacer JOIN FETCH y poder traer también sus etiquetas y descuentos
            // sin el FETCH al cerrar el EM al leer la etiqueta fuera de aquí el programa daría una exception
            return em.createQuery(
                            "SELECT p FROM Producto p " +
                                    "LEFT JOIN FETCH p.etiqueta " +
                                    "LEFT JOIN FETCH p.descuento " +
                                    "WHERE p.id = :id", Producto.class)
                    // evitamos inyección SQL con parámetros
                    .setParameter("id", id)
                    // convertimos resultado a Stream u cogemos el primero
                    .getResultStream()
                    .findFirst();
        }
    }

    // metodo que devuelve todos los productos
    @Override
    public List<Producto> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            // no usar un simple select, usar JOIN FETCH para cargar etiqueta y descuento
            return em.createQuery(
                    // distinct para evitar duplicados por el join
                    "SELECT DISTINCT p FROM Producto p " +
                            "LEFT JOIN FETCH p.etiqueta " +
                            "LEFT JOIN FETCH p.descuento", Producto.class
            ).getResultList();
        }
    }

    // metodo para actualizar un producto
    @Override
    public void update(Producto producto) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // validar existencia (vemos que el producto tiene ID), para este EM nuevo entá en estado DETACHED (no lo conoce)
        Producto existente = em.find(Producto.class, producto.getId());
        if (existente == null) {
            em.close();
            throw new SQLException("No existe el producto para actualizar");
        }

        em.getTransaction().begin();
        // busca el registro por ID, lo marca como "sucio" para que deba actualizarse
        em.merge(producto);
        // JPA genera un SQL de update comparando los campos que cambiaron
        em.getTransaction().commit();
        // cierre manual
        em.close();
    }

    // metodo para borrar un producto por id
    @Override
    public void delete(long id) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // buscar el objeto, si existe JPA lo gestiona (MANAGED)
        Producto p = em.find(Producto.class, id);

        // validación de existencia
        if (p == null) {
            em.close();
            throw new SQLException("No existe el producto para borrar");
        }

        em.getTransaction().begin();
        // se marca para eliminar (REMOVED)
        em.remove(p);
        // JPA lanza consulta de delete para borrarlo de la base de datos
        em.getTransaction().commit();
        em.close();
    }


    // METODOS ESPECÍFICOS
    // metodo que devuelve una lista de los usuarios que tienen un producto concreto (por id) en favoritos
    @Override
    public List<Usuario> findUsuariosFavoritos(long productoId) throws SQLException {
        try (EntityManager em = emf.createEntityManager()) {
            // cargar el producto
            Producto p = em.find(Producto.class, productoId);
            // validación de existencia
            if (p == null) {
                throw new SQLException("Producto no existe");
            }

            // copia en memoria de Java con datos cargados para poder usar los datos en el controlador
            return new ArrayList<>(p.getUsuariosProductosFavoritos());
        }
    }

    // metodo para agregar un producto concreto a favoritos de un usuario concreto
    @Override
    public List<Usuario> agregarFavorito(long productoId, long usuarioId) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // buscamos ambos objetos (cargamos para que estén en estado MANAGED)
        Producto p = em.find(Producto.class, productoId);
        Usuario u = em.find(Usuario.class, usuarioId);

        // comprobamos existencia, si alguno es null lanza SQL exception
        if (p == null || u == null) {
            em.close();
            throw new SQLException("Usuario o producto no existe");
        }

        em.getTransaction().begin();
        // añadimos el producto a la lista de favoritos del usuario en Java
        u.getFavoritos().add(p);
        // JPA detecta que hay un nuevo elemento en la lista y genera automáticamente un intert en la tabla usuarios_favoritos
        em.merge(u);
        em.getTransaction().commit();

        // usar una copia de la lista de productos favoritos antes de cerrar el EM
        List<Usuario> usuariosQueLoTienen = new ArrayList<>(p.getUsuariosProductosFavoritos());
        em.close();
        return usuariosQueLoTienen;
    }

    // metodo para eliminar un producto concreto de favoritos de un usuario concreto
    @Override
    public void eliminarFavorito(long productoId, long usuarioId) throws SQLException {
        try (EntityManager em = emf.createEntityManager()) {

            // cargar objetos (estado MANAGED)
            Producto p = em.find(Producto.class, productoId);
            Usuario u = em.find(Usuario.class, usuarioId);

            // validación de existencia
            if (p == null || u == null) {
                throw new SQLException("No se puede eliminar: Producto o Usuario inexistente");
            }

            em.getTransaction().begin();
            // eliminar el producto de la lista (estado REMOVED)
            u.getFavoritos().remove(p);
            // JPA compara la lista vieja con la nueva y comprueba que falta "p", asique lanza automáticamente un delete a la BD
            em.merge(u);
            em.getTransaction().commit();
        }
    }
}
