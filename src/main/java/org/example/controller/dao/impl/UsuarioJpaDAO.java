package org.example.controller.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.sql.*;
import java.util.List;
import java.util.Optional;

// implementación del DAO, aquí es donde se debe utilizar la tecnología específica de base de datos como JDBC o JPA
public class UsuarioJpaDAO implements UsuarioDAO {

    // usar singleton en los dao, sirve para que solo haya una instancia de una clase en toda la aplicacion, es un punto global de acceso a esa instancia
    // se usa en los dao para que solo haya una implementación que gestione el acceso a la base de datos
    // referencia estática
    private static UsuarioJpaDAO instance;

    //
    private final EntityManagerFactory emf;

    private UsuarioJpaDAO() {
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }

    public static UsuarioJpaDAO getInstance() {
        if (instance == null) instance = new UsuarioJpaDAO();
        return instance;
    }


    // CRUD
    // metodo de insertar usuario a la base de datos
    @Override
    public boolean save(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // metodo para encontrar un usuario mediante su id
    @Override
    public Optional<Usuario> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Usuario.class, id));
        } finally {
            em.close();
        }
    }

    // metodo que devuelve todos los usuarios
    @Override
    public List<Usuario> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u", Usuario.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // metodo para actualizar un usuario
    @Override
    public boolean update(Usuario usuario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // metodo para borrar un usuario
    @Override
    public boolean delete(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Usuario u = em.find(Usuario.class, id);
            if (u == null) return false;

            em.getTransaction().begin();
            em.remove(u);
            em.getTransaction().commit();
            return true;
        } finally {
            em.close();
        }
    }


    // METODOS ESPECÍFICOS
    // metodo para buscar un usuario por email
    @Override
    public Optional<Usuario> findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    // metodo se obtienen los productos favoritos de un usuario
    @Override
    public List<Producto> findFavoritos(long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            Usuario u = em.find(Usuario.class, usuarioId);
            if (u == null) throw new RuntimeException("Usuario no existe");
            return (List<Producto>) u.getFavoritos();
        } finally {
            em.close();
        }
    }
}

