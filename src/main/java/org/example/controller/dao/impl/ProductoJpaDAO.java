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

public class ProductoJpaDAO implements ProductoDAO {
    // campo estático que contendrá la única instancia del DAO
    private static ProductoJpaDAO instance;

    //
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
        if (producto.getNombre() == null) {
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }

        EntityManager em = emf.createEntityManager();

        // Cambiamos la lógica: si el ID es 0, es que no se ha asignado ID aún.
        // Pero el test fuerza un ID 99999, así que comprobamos si existe en la BD.
        if (producto.getEtiqueta() != null && producto.getEtiqueta().getId() != 0) {
            Etiqueta e = em.find(Etiqueta.class, producto.getEtiqueta().getId());
            if (e == null) {
                em.close();
                throw new SQLException("La etiqueta con ID " + producto.getEtiqueta().getId() + " no existe");
            }
        }

        em.getTransaction().begin();
        em.persist(producto);
        em.getTransaction().commit();
        em.close();
        return producto;
    }

    // metodo para encontrar un producto en concreto por id
    @Override
    public Optional<Producto> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Producto p " +
                                    "LEFT JOIN FETCH p.etiqueta " +
                                    "LEFT JOIN FETCH p.descuento " +
                                    "WHERE p.id = :id", Producto.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }


    // metodo que devuelve todos los productos
    @Override
    public List<Producto> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            // Usamos JOIN FETCH para traer etiqueta y descuento en un solo SELECT
            // DISTINCT evita que salgan productos duplicados por los joins
            return em.createQuery(
                    "SELECT DISTINCT p FROM Producto p " +
                            "LEFT JOIN FETCH p.etiqueta " +
                            "LEFT JOIN FETCH p.descuento", Producto.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // metodo para actualizar un producto
    @Override
    public void update(Producto producto) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // 3. Validar existencia para el test: actualizarProductoInexistenteIncorrecto
        Producto existente = em.find(Producto.class, producto.getId());
        if (existente == null) {
            em.close();
            throw new SQLException("No existe el producto para actualizar");
        }

        em.getTransaction().begin();
        em.merge(producto);
        em.getTransaction().commit();
        em.close();
    }

    // metodo para borrar un producto por id
    @Override
    public void delete(long id) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // 4. Validar para el test: borrarProductoIncorrecto
        Producto p = em.find(Producto.class, id);
        if (p == null) {
            em.close();
            throw new SQLException("No existe el producto para borrar");
        }

        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();
        em.close();
    }

    // METODOS ESPECÍFICOS
    // metodo que devuelve una lista de los usuarios que tienen un producto concreto (por id) en favoritos
    @Override
    public List<Usuario> findUsuariosFavoritos(long productoId) throws SQLException {
        EntityManager em = emf.createEntityManager();
        try {
            Producto p = em.find(Producto.class, productoId);

            // El test falla porque aquí tenías "RuntimeException"
            if (p == null) {
                throw new SQLException("Producto no existe");
            }

            // Recuerda usar new ArrayList para evitar el error de casteo de Set a List
            return new ArrayList<>(p.getUsuariosProductosFavoritos());
        } finally {
            em.close();
        }
    }

    // metodo para agregar un producto concreto a favoritos de un usuario concreto
    @Override
    public List<Usuario> agregarFavorito(long productoId, long usuarioId) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // 1. Buscamos ambos objetos
        Producto p = em.find(Producto.class, productoId);
        Usuario u = em.find(Usuario.class, usuarioId);

        // 2. Comprobamos existencia.
        // Si no existen, lanzamos SQLException para que el test se ponga en verde
        if (p == null || u == null) {
            em.close();
            throw new SQLException("Usuario o producto no existe");
        }

        // 3. Si existen, realizamos la operación
        em.getTransaction().begin();
        u.getFavoritos().add(p);
        em.merge(u);
        em.getTransaction().commit();
        // 4. IMPORTANTE: Devolvemos la lista de usuarios que tienen este producto como favorito
        // Usamos el constructor de ArrayList para convertir el Set y evitar errores de casteo
        List<Usuario> usuariosQueLoTienen = new ArrayList<>(p.getUsuariosProductosFavoritos());

        em.close();
        return usuariosQueLoTienen;
    }

    // metodo para eliminar un producto concreto de favoritos de un usuario concreto
    @Override
    public void eliminarFavorito(long productoId, long usuarioId) throws SQLException {
        EntityManager em = emf.createEntityManager();

        // 1. Buscamos el producto y el usuario
        Producto p = em.find(Producto.class, productoId);
        Usuario u = em.find(Usuario.class, usuarioId);

        // 2. VALIDACIÓN PARA EL TEST: Si el producto o usuario no existen, lanzamos la excepción
        if (p == null || u == null) {
            em.close();
            throw new SQLException("No se puede eliminar: Producto o Usuario inexistente");
        }

        // 3. Si existen, procedemos con la lógica de borrado
        em.getTransaction().begin();

        // Eliminamos de la colección (JPA se encarga de la tabla intermedia al hacer commit)
        u.getFavoritos().remove(p);

        em.merge(u);
        em.getTransaction().commit();

        // 4. Devolvemos la lista actualizada (convertida a ArrayList para evitar problemas de tipos)
        List<Usuario> usuariosRestantes = new ArrayList<>(p.getUsuariosProductosFavoritos());

        em.close();
    }
}
