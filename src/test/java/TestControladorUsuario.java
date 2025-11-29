
import org.example.controller.ControladorProducto;
import org.example.controller.ControladorUsuario;
import org.example.controller.dao.impl.JdbcProductoDAO;
import org.example.controller.dao.impl.JdbcUsuarioDAO;
import org.example.controller.dao.interfaces.UsuarioDAO;
import org.example.model.Usuario;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.utils.DatabaseConf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestControladorUsuario {
    private ControladorUsuario controladorUsuario;
    private ControladorProducto controladorProducto = new ControladorProducto();
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioInicial;


    // este bloque se repetirá cada vez que se haga un test
    @BeforeEach
    void setUp() throws Exception {
        // reiniciar la BD a estado conocido
        DatabaseConf.dropAndCreateTables();

        // reset singletons / estado en memoria para tests
        JdbcUsuarioDAO.resetForTests();
        JdbcProductoDAO.resetForTests();

        // crear controlador que usa el DAO
        usuarioDAO = JdbcUsuarioDAO.getInstance();
        controladorUsuario = new ControladorUsuario();

        // crear y registrar un usuario base para cada test
        usuarioInicial = new Usuario(
                "Sandra",
                "11111111A",
                "Alguna calle",
                LocalDate.of(2005, 9, 12),
                "600123123",
                "sandra@gmail.com",
                "1234"
        );

        // registrar usando el controlador (que delega en el DAO)
        // puede lanzar SQLException si algo falla
        controladorUsuario.registrarUsuario(usuarioInicial);
        // tras registrar el usuario inicial, su id debe estar asignado por el DAO/DB
        assertTrue(usuarioInicial.getId() > 0, "El id debe asignarse tras registrar");
    }

    // SAVE
    @Test
    void registrarUsuarioCorrecto() throws SQLException {
        // creamos otro usuario para comprobar que funciona el registro (distinto al creado anteriormente,e ste no nos vale porque ya está registrado)
        Usuario usuario2 = new Usuario(
                "Pepe",
                "22222222B",
                "Otra calle",
                LocalDate.of(1990, 1, 1),
                "600000000",
                "pepe@example.com",
                "12345"
        );
        // registramos el usuario
        controladorUsuario.registrarUsuario(usuario2);

        // comprobar que está en la BD (findByEmail)
        Optional<Usuario> encontrado = usuarioDAO.findByEmail("pepe@example.com");
        assertTrue(encontrado.isPresent());
        assertEquals("Pepe", encontrado.get().getNombre());
    }

    //repetir email
    @Test
    void registrarUsuarioIncorrecto() {
        // intentar registrar otro usuario con mismo email (la BD tiene UNIQUE sobre email, por lo que se espera exception)
        Usuario duplicado = new Usuario(
                "Otra",
                "33333333C",
                "Calle X",
                LocalDate.of(1995, 2, 2),
                "611111111",
                // mismo email de usuarioInicial
                "sandra@gmail.com",
                "pw2"
        );

        // comprobamos que da exception al existir el email ya
        assertThrows(Exception.class, () -> controladorUsuario.registrarUsuario(duplicado));
    }

    // FIND BY ID
    @Test
    void findByIdCorrecto() throws SQLException {
        // obtenemos el usuario con la id del usuario inicial (el que ya tenemos creado)
        Optional<Usuario> optional = controladorUsuario.obtenerPorId(usuarioInicial.getId());
        // comprobamos que tiene datos la variable
        assertTrue(optional.isPresent());
        // y comprobamos que tiene el mismo email que el usuario inicial
        assertEquals(usuarioInicial.getEmail(), optional.get().getEmail());
    }

    @Test
    void findByIdIncorrecto() {
        assertThrows(IllegalArgumentException.class,
                // intentamos buscar un usuario con id inexistente
                () -> controladorUsuario.obtenerPorId(99999));
    }

    // FIND ALL
    @Test
    void FindAllCorrecto() throws SQLException {
        // obtenemos todos los usuarios
        var usuarios = controladorUsuario.obtenerTodos();
        // y comprobamos que solo hay uno
        assertEquals(1, usuarios.size());
    }

    // FIND BY EMAIL
    @Test
    void findByEmailCorrecto() throws Exception {
        // guardamos en una variable el resultado de la búsqueda de un usuario por email
        Optional<Usuario> usuPorEmail = Optional.ofNullable(controladorUsuario.obtenerPorEmail(usuarioInicial.getEmail()));
        // observamos si es tiene contenido la variable
        assertTrue(usuPorEmail.isPresent());
        // comprobamos que son iguales los email
        assertEquals(usuarioInicial.getEmail(), usuPorEmail.get().getEmail());
    }

    @Test
    void findByEmailInexistenteIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> {
            // intentamos buscar con un email inexistente y da error
            controladorUsuario.obtenerPorEmail("emailInexistente@correo.com");
        });
    }

    // UDDATE
    @Test
    void updateUsuarioCorrecto() throws Exception {
        // le cambiamos el telefono y después actualizamos
        usuarioInicial.setTelefono("777777777");
        controladorUsuario.actualizarUsuario(usuarioInicial);
        // obtenemos el resultado de buscar por id el usuario (con el telefono cambiado)
        Usuario actualizado =
                controladorUsuario.obtenerPorId(usuarioInicial.getId()).orElseThrow();
        // y comprobamos que se ha cambiado el telefono
        assertEquals("777777777", actualizado.getTelefono());
    }

    @Test
    void updateUsuarioInexistenteIncorrecto() {
        // creamos usuario
        Usuario noExiste = new Usuario(
                "AAA", "44444444D", "X",
                LocalDate.of(2000, 1, 1), "600000000",
                "x@gmail.com", "1234"
        );
        // le ponemos id no registrada
        noExiste.setId(99999);
        // intentamos actualizar
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.actualizarUsuario(noExiste));
    }

    @Test
    void updateUsuarioCampoNoEditableIncorrecto() {
        // intentamos cambiar el dni (campo ineditable) y al actualizar nos da error
        usuarioInicial.setDni("00000000Z");
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.actualizarUsuario(usuarioInicial));
    }

    // DELETE
    @Test
    void deleteUsuarioCorrecto() throws Exception {
        // eliminamos usuario
        controladorUsuario.eliminarUsuario(usuarioInicial.getId());
        // comprobamos que al contarlos todos, no hay ninguno
        long count = controladorUsuario.obtenerTodos().spliterator().getExactSizeIfKnown();
        assertEquals(0, count);
    }

    @Test
    void deleteUsuarioInexistenteIncorrecto() {
        // eliminar usuario con id no registrada
        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.eliminarUsuario(99999);
        });
    }

    // LOGIN
    @Test
    void loginCorrecto() throws Exception {
        // hacemos login con una cuenta registrada
        Optional<Usuario> usuLogin = controladorUsuario.login("sandra@gmail.com", "1234");
        assertTrue(usuLogin.isPresent());
        assertEquals(usuarioInicial.getEmail(), usuLogin.get().getEmail());
    }

    @Test
    void loginEmailIncorrecto() {
        // loguear con email o contraseña no correctos
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.login("noexiste@gmail.com", "1234"));

        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.login(usuarioInicial.getEmail(), "xxxx"));
    }

    // FAVORITOS
    @Test
    void agregarYObtenerFavoritosCorrecto() throws Exception {
        // crear producto con etiqueta
        Etiqueta etiqueta = new Etiqueta("Nueva");
        Producto camisa = new Camisa(
                "Camisa Casual",
                "MarcaX",
                29.99,
                Talla.M,
                Color.AZUL,
                etiqueta,
                2
        );
        // metemos el producto en la base de datos
        controladorProducto.crearProducto(camisa);
        // lo agregamos como favorito del usuario inicial
        controladorUsuario.agregarFavorito(camisa, usuarioInicial);
        // obtenemos los favoritos del usuario inicial
        List<Producto> favoritos = controladorUsuario.obtenerFavoritos(usuarioInicial.getId());
        // comprobamos que está el producto creado
        assertTrue(favoritos.stream().anyMatch(p -> p.getId() == camisa.getId()));
    }

    @Test
    void agregarYObtenerFavoritosIncorrecto() {
        // obtenemos favoritos de id de usuario no registrado
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.obtenerFavoritos(99999));
    }

    @Test
    void eliminarFavoritoCorrecto() throws Exception {
        // creamos producto
        Etiqueta etiqueta = new Etiqueta("Nueva");
        Producto camisa = new Camisa(
                "Camisa Casual",
                "MarcaX",
                29.99,
                Talla.M,
                Color.AZUL,
                etiqueta,
                2
        );
        // mismo procedimiento que en agregar a favoritos pero eliminandolo antes de comprobar
        controladorProducto.crearProducto(camisa);
        controladorUsuario.agregarFavorito(camisa, usuarioInicial);
        controladorUsuario.eliminarFavorito(camisa, usuarioInicial);
        List<Producto> favoritos = controladorUsuario.obtenerFavoritos(usuarioInicial.getId());
        assertFalse(favoritos.stream().anyMatch(p -> p.getId() == camisa.getId()));
    }

    @Test
    void eliminarFavoritoIncorrecto() {
        // creamos producto
        Etiqueta etiqueta = new Etiqueta("Nueva");
        Producto camisa = new Camisa(
                "Camisa Casual",
                "MarcaX",
                29.99,
                Talla.M,
                Color.AZUL,
                etiqueta,
                2
        );
        // el producto NO está registrado, debe fallar
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.eliminarFavorito(camisa, usuarioInicial));
    }

}

    


            
