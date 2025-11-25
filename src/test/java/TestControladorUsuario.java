
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
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioInicial;
    // usar funciones de producto necesarias en tests
    private JdbcProductoDAO productoDAO;
    private Producto camisa;


    // este bloque se repetirá cada vez que se haga un test
    @BeforeEach
    void setUp() throws Exception {
        // reiniciar la BD a estado conocido
        DatabaseConf.dropAndCreateTables();

        // reset singletons / estado en memoria para tests
        JdbcUsuarioDAO.resetForTests();
        JdbcProductoDAO.resetForTests();

        // crear controlador que usa el DAO
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

    // SAVE/CREAR
    //cuidado, no repetir email
    @Test
    void registrarUsuarioCorrecto() throws SQLException {
        Usuario usuario2 = new Usuario(
                "Pepe",
                "22222222B",
                "Otra calle",
                LocalDate.of(1990, 1, 1),
                "600000000",
                "pepe@example.com",
                "12345"
        );
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

        assertThrows(Exception.class, () -> controladorUsuario.registrarUsuario(duplicado));
    }

    // FIND BY ID
    @Test
    void findByIdCorrecto() throws SQLException {
        Optional<Usuario> opt = controladorUsuario.obtenerPorId(usuarioInicial.getId());
        assertTrue(opt.isPresent());
        assertEquals(usuarioInicial.getEmail(), opt.get().getEmail());
    }

    @Test
    void findByIdIncorrecto() {
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.obtenerPorId(99999));
    }

    // FIND ALL
    @Test
    void FindAllCorrecto() throws SQLException {
        assertEquals(1, controladorUsuario.obtenerTodos().size());
    }

    // FIND BY EMAIL
    @Test
    void findByEmailCorrecto() throws Exception {
        // ????
    }

    @Test
    void findByEmailInexistenteIncorrecto() throws Exception {
        // ????
    }

    // UDDATE
    @Test
    void updateUsuarioCorrecto() throws Exception {
        usuarioInicial.setTelefono("777777777");
        controladorUsuario.actualizarUsuario(usuarioInicial);
        Usuario actualizado =
                controladorUsuario.obtenerPorId(usuarioInicial.getId()).orElseThrow();
        assertEquals("777777777", actualizado.getTelefono());
    }

    @Test
    void updateUsuarioInexistenteIncorrecto() throws Exception {
        Usuario noExiste = new Usuario(
                "AAA", "44444444D", "X",
                LocalDate.of(2000, 1, 1), "600000000",
                "x@gmail.com", "1234"
        );
        noExiste.setId(99999);
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.actualizarUsuario(noExiste));
    }

    @Test
    void updateUsuarioCampoNoEditableIncorrecto() {
        usuarioInicial.setDni("00000000Z");
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.actualizarUsuario(usuarioInicial));
    }

    // DELETE
    @Test
    void deleteUsuarioCorrecto() throws Exception {
        controladorUsuario.eliminarUsuario(usuarioInicial.getId());
        long count = controladorUsuario.obtenerTodos().spliterator().getExactSizeIfKnown();
        assertEquals(0, count);
    }

    @Test
    void deleteUsuarioInexistenteIncorrecto() throws Exception {
        // ????
    }

    // LOGIN
    @Test
    void loginCorrecto() throws Exception {
        Optional<Usuario> logged = controladorUsuario.login("sandra@gmail.com", "1234");
        assertTrue(logged.isPresent());
        assertEquals(usuarioInicial.getEmail(), logged.get().getEmail());
    }

    @Test
    void loginEmailIncorrecto() throws SQLException {
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.login("noexiste@gmail.com", "1234"));

        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.login(usuarioInicial.getEmail(), "xxxx"));
    }

    // FAVORITOS
    @Test
    void agregarYObtenerFavoritosCorrecto() throws Exception {
        Etiqueta etiqueta = new Etiqueta("Nueva");
        camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
        controladorUsuario.agregarFavorito(camisa, usuarioInicial);
        assertTrue(controladorUsuario.obtenerFavoritos(usuarioInicial).contains(camisa));
    }

    @Test
    void agregarYObtenerFavoritosIncorrecto() {
        assertThrows(IllegalArgumentException.class,
                () -> controladorUsuario.obtenerFavoritos(99999));
    }

    @Test
    void eliminarFavoritoCorrecto() throws Exception {
        Etiqueta etiqueta = new Etiqueta("Nueva");
        camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
        controladorUsuario.getControladorProducto().registrarProducto(camisa);
        controladorUsuario.agregarFavorito(camisa, usuarioInicial);

        controladorUsuario.eliminarFavorito(camisa, usuarioInicial);

        assertFalse(controladorUsuario.obtenerFavoritos(usuarioInicial).contains(camisa));
    }
}

@Test
void eliminarFavoritoIncorrecto() {
    Etiqueta etiqueta = new Etiqueta("Nueva");
    camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
    assertThrows(IllegalArgumentException.class,
            () -> controladorUsuario.eliminarFavorito(camisa, usuarioInicial));
}

    }


            }
