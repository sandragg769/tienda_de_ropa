
import org.example.controller.ControladorProducto;
import org.example.controller.ControladorUsuario;
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

class TestControladorProducto {
    private ControladorProducto controladorProducto;
    private ControladorUsuario controladorUsuario;

    private Producto camisa;
    private Etiqueta etiqueta;
    private Usuario usuario;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConf.dropAndCreateTables();

        controladorProducto = new ControladorProducto();
        controladorUsuario = new ControladorUsuario();

        // Usuario para pruebas de favoritos
        usuario = new Usuario(
                "Sandra", "11111111A", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123",
                "sandra@gmail.com", "1234"
        );
        controladorUsuario.registrarUsuario(usuario);

        etiqueta = new Etiqueta("NOVEDAD");
        camisa = new Camisa(
                "Camisa Azul", "Zara", 29.99,
                Talla.M, Color.AZUL, etiqueta, 2
        );
    }

    // CRUD
    @Test
    void crearProductoCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);

        List<Producto> productos = controladorProducto.obtenerTodos();
        System.out.println(productos);

        assertEquals(1, productos.size());
    }

    @Test
    void crearProductoConDatosInvalidosIncorrecto() {
        Producto invalido = new Camisa(
                null, "Zara", 29.99,
                Talla.M, Color.AZUL, etiqueta, 2
        );

        assertThrows(IllegalArgumentException.class,
                () -> controladorProducto.crearProducto(invalido));
    }

    @Test
    void crearProductoEtiquetaFKInexistenteLanzaSQLException() {
        // Creamos un producto con etiqueta ya persistida, pero manipulamos el id para forzar fallo
        Etiqueta etiqueta = new Etiqueta("NUEVA");
        etiqueta.setId(99999L); // NO existe en la base → rompe la foreign key

        Producto camisa = new Camisa(
                "Camisa Error", "MarcaX", 20.0,
                Talla.M, Color.AZUL,
                etiqueta, 3
        );

        assertThrows(SQLException.class, () -> controladorProducto.crearProducto(camisa));
    }

    @Test
    void buscarPorIdCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);
        Optional<Producto> encontrado = controladorProducto.buscarPorId(camisa.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Camisa Azul", encontrado.get().getNombre());
    }

    @Test
    void buscarPorIdIncorrecto() throws SQLException {
        Optional<Producto> encontrado = controladorProducto.buscarPorId(99999);
        assertTrue(encontrado.isEmpty());
    }

    @Test
    void findAllCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);
        assertEquals(1, controladorProducto.obtenerTodos().size());
    }


    @Test
    void actualizarProductoCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);

        camisa.setPrecioInicial(49.99);
        controladorProducto.actualizarProducto(camisa);

        Producto actualizado = controladorProducto.buscarPorId(camisa.getId()).get();
        assertEquals(49.99, actualizado.getPrecioInicial());
    }

    @Test
    void actualizarProductoInexistenteIncorrecto() {
        Producto inexistente = new Camisa(
                "Otro", "Marca", 15.0,
                Talla.L, Color.ROJO, etiqueta, 3
        );
        inexistente.setId(999);

        assertThrows(SQLException.class,
                () -> controladorProducto.actualizarProducto(inexistente));
    }


    @Test
    void borrarProductoCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);
        controladorProducto.borrarProducto(camisa.getId());

        assertEquals(0, controladorProducto.obtenerTodos().size());
    }

    @Test
    void borrarProductoIncorrecto() {
        assertThrows(SQLException.class, () -> controladorProducto.borrarProducto(99999));
    }


    // FAVORITOS
    @Test
    void agregarFavoritoCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);

        controladorProducto.agregarFavorito(camisa.getId(), usuario.getId());

        List<Usuario> usuarios = controladorProducto.obtenerUsuariosFavoritoDeProducto(camisa.getId());
        assertEquals(1, usuarios.size());
    }

    @Test
    void agregarFavoritoProductoNoExistenteIncorrecto() {
        assertThrows(SQLException.class, () ->
                controladorProducto.agregarFavorito(99999, usuario.getId()));
    }

    @Test
    void obtenerUsuariosFavoritosCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);
        controladorProducto.agregarFavorito(camisa.getId(), usuario.getId());

        List<Usuario> fav = controladorProducto.obtenerUsuariosFavoritoDeProducto(camisa.getId());
        assertTrue(fav.contains(usuario));
    }

    @Test
    void obtenerUsuariosFavoritosIncorrecto() {
        assertThrows(SQLException.class,
                () -> controladorProducto.obtenerUsuariosFavoritoDeProducto(99999));
    }

    @Test
    void eliminarFavoritoCorrecto() throws SQLException {
        controladorProducto.crearProducto(camisa);
        controladorProducto.agregarFavorito(camisa.getId(), usuario.getId());

        controladorProducto.eliminarFavorito(camisa.getId(), usuario.getId());

        List<Usuario> usuarios = controladorProducto.obtenerUsuariosFavoritoDeProducto(camisa.getId());
        assertEquals(0, usuarios.size());
    }

    @Test
    void eliminarFavoritoIncorrecto() {
        assertThrows(SQLException.class,
                () -> controladorProducto.eliminarFavorito(99999, usuario.getId()));
    }


}
