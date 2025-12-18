package org.example.controller.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.controller.dao.interfaces.ProductoDAO;
import org.example.model.Usuario;
import org.example.model.descuento.Descuento;
import org.example.model.descuento.DescuentoFijo;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Chaqueta;
import org.example.model.producto.tipo_de_productos.Pantalon;
import org.example.utils.DatabaseConf;

import java.sql.*;
import java.time.LocalDate;
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
    public Producto save(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // cascada etiqueta + descuento
            em.persist(producto);
            em.getTransaction().commit();
            return producto;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // metodo para encontrar un producto en concreto por id
    @Override
    public Optional<Producto> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Producto.class, id));
        } finally {
            em.close();
        }
    }


    // metodo que devuelve todos los productos
    @Override
    public List<Producto> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Producto p", Producto.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // metodo para actualizar un producto
    @Override
    public void update(Producto producto) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(producto);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // metodo para borrar un producto por id
    @Override
    public void delete(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Producto p = em.find(Producto.class, id);
            if (p == null) throw new RuntimeException("Producto no existe");

            em.getTransaction().begin();
            em.remove(p);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // METODOS ESPECÍFICOS
    // metodo que devuelve una lista de los usuarios que tienen un producto concreto (por id) en favoritos
    @Override
    public List<Usuario> findUsuariosFavoritos(long productoId) {
        EntityManager em = emf.createEntityManager();
        try {
            Producto p = em.find(Producto.class, productoId);
            if (p == null) throw new RuntimeException("Producto no existe");
            return (List<Usuario>) p.getUsuariosProductosFavoritos();
        } finally {
            em.close();
        }
    }

    // metodo para agregar un producto concreto a favoritos de un usuario concreto
    @Override
    public List<Usuario> agregarFavorito(long productoId, long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            Producto p = em.find(Producto.class, productoId);
            Usuario u = em.find(Usuario.class, usuarioId);

            if (p == null || u == null) {
                throw new RuntimeException("Usuario o producto no existe");
            }

            em.getTransaction().begin();
            u.getFavoritos().add(p);
            em.merge(u);
            em.getTransaction().commit();

            return (List<Usuario>) p.getUsuariosProductosFavoritos();
        } finally {
            em.close();
        }
    }

    // metodo para eliminar un producto concreto de favoritos de un usuario concreto
    @Override
    public void eliminarFavorito(long productoId, long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            Producto p = em.find(Producto.class, productoId);
            Usuario u = em.find(Usuario.class, usuarioId);

            em.getTransaction().begin();
            u.getFavoritos().remove(p);
            em.merge(u);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
