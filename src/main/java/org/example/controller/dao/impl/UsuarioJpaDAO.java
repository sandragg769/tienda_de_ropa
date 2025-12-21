package org.example.controller.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// implementación del DAO, aquí es donde se debe utilizar la tecnología específica de base de datos como JDBC o JPA
public class UsuarioJpaDAO implements UsuarioDAO {

    // usar singleton en los dao, sirve para que solo haya una instancia de una clase en toda la aplicacion, es un punto global de acceso a esa instancia
    // se usa en los dao para que solo haya una implementación que gestione el acceso a la base de datos
    // referencia estática
    private static UsuarioJpaDAO instance;

    // (fábrica de gestores de entidades) es un objeto que lee el persistence.xml
    private EntityManagerFactory emf;

    // constructor privado, impide que se haga un "new UsuarioJpaDAo(" desde fuera
    private UsuarioJpaDAO() {
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }

    // punto de acceso global
    public static UsuarioJpaDAO getInstance() {
        // si no existe la instancia la crea, si existe la devuelve
        if (instance == null) instance = new UsuarioJpaDAO();
        return instance;
    }

    // cierra la fábrica actual y la reinicia, se usa para los test para que la base de datos esté limpia entre ejecuciones
    public void reset() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
        this.emf = Persistence.createEntityManagerFactory("tiendaRopa-jpa");
    }


    // CRUD
    // metodo de insertar usuario a la base de datos
    @Override
    public boolean save(Usuario usuario) {
        // con el try-with-resources el EntityManager se crea aquí y garantiza que se cierra al llegar al final del bloque
        try (EntityManager em = emf.createEntityManager()) {
            // iniciamos la transacción, para poder ESCRIBIR en la base de datos
            em.getTransaction().begin();
            // el objeto entra siendo TRANSIENT (solo existe en memoria de Java),
            // al hacer persist pasa a estado MANAGED (gestionado) ,
            // ahora JPA sabe que tiene que guardarlo pero todavía no lo ha hecho
            em.persist(usuario);
            // si hay un error, la excepción saltará aquí mismo y saldrá del metodo automáticamente
            // si no hay error revisa los objetos MANAGED para generar el SQL y enviarlo a la base de datos
            em.getTransaction().commit();
            // si el código llega aquí, es que tod ha ido bien
            return true;
        }
    }

    // metodo para encontrar un usuario mediante su id
    @Override
    public Optional<Usuario> findById(long id) {
        // con el try-with-resources java cierra automáticamente el EntityManager al terminar el bloque
        try (EntityManager em = emf.createEntityManager()) {
            // JPA busca en su caché interna si lo encuentra crea un objeto de Java y lo pone en MANAGED,
            // si no existe devuelve null es decir un empty por el Optional
            return Optional.ofNullable(em.find(Usuario.class, id));
        }
    }

    // metodo que devuelve todos los usuarios
    @Override
    public List<Usuario> findAll() {
        // todos los metodos usan esto ya que es lo más cómodo
        try (EntityManager em = emf.createEntityManager()) {
            // usamos JPQL para consultar sobre la clase Usuario no sobre la tabla,
            // devolvemos la lista de todos los usuarios MANAGED
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
        }
    }

    // metodo para actualizar un usuario
    @Override
    public boolean update(Usuario usuario) {
        try (EntityManager em = emf.createEntityManager()) {
            // iniciamos la transacción ya que vamos a ESCRIBIR (toda modificación (merge) debe ir dentro de una)
            em.getTransaction().begin();
            // el usuario que recibimos viene de fuera, es un objeto DETACHED (este EntityManager nuevo no lo conoce),
            // con el merge buscamos el usuario en la base de datos y copiamos los datos del objeto que le pasamos al objeto de la BD
            em.merge(usuario);
            // confirmamos los cambios, si hay un error saltará una excepción aquí y el metodo se interrumpirá
            em.getTransaction().commit();
            // si llegamos aquí es que la actualización ha funcionado
            return true;
        }
    }

    // metodo para borrar un usuario
    @Override
    public boolean delete(long id) {
        try (EntityManager em = emf.createEntityManager()) {
            // encontrar usuario a borrar por id, obtiene un objeto MANAGED (gestionado)
            Usuario u = em.find(Usuario.class, id);
            // si no encuentra se sale del metodo sin hacer nada
            if (u == null) return false;

            // transacción al ESCRIBIR en la base de datos
            em.getTransaction().begin();
            // solo se pueden borrar objetos MANAGED, cambiamos el estado del objeto a REMOVED, todavía existe en la base de datos (está marcado para morir por así decirlo) pero en Java ya no
            em.remove(u);
            // si sale un error volverá a como estaba antes, antes de la transacción
            // JPA mira si hay objetos REMOVED y lanza un comando a la base de datos para borrar
            em.getTransaction().commit();

            // si llega aquí se borró correctamente
            return true;
        }
    }


    // METODOS ESPECÍFICOS
    // metodo para buscar un usuario por email
    @Override
    public Optional<Usuario> findByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            // creamos consulta JPQL, :email parámetro nombrado
            return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    // asignamos el valor de forma segura
                    .setParameter("email", email)
                    // convierte el resultado en un flujo de datos
                    .getResultStream()
                    // devuelve un Optional , si no encuentra devuelve empty y si encuentra lo devuelve
                    .findFirst();
        }
    }

    // metodo se obtienen los productos favoritos de un usuario
    @Override
    public List<Producto> findFavoritos(long usuarioId) {
        try (EntityManager em = emf.createEntityManager()) {
            // cargamos el usuario, estado MANAGED
            Usuario u = em.find(Usuario.class, usuarioId);
            // comprobamos si se encuentra el usuario o no
            if (u == null) throw new RuntimeException("Usuario no existe");
            // si se ha encontrado usuario, tenemos que crear una copia independiente en la memoria de Java,
            // obliga a JPA a traer todos los datos de la BD y guardarlos en una
            // lista normal de Java, si no se hiciera así se devolvería una lista con la sesión cerrada
            // y al intentar leerla en el controlador daría error
            return new ArrayList<>(u.getFavoritos());
        }
    }
}

