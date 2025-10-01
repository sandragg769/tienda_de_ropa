
import org.example.controller.ControladorUsuario;
import org.example.model.Usuario;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TestControladorUsuario {
    private ControladorUsuario controladorUsuario;
    //este lo usaremos común para los distintos test, así se declara una vez solo
    private Usuario usuario;
    //para los test de favoritos necesitamos productos
    private Producto camisa;
    private Etiqueta etiqueta;


    // este bloque se repetirá cada vez que se haga un test
    @BeforeEach
    void setUp() {
        //siempre hay que crear el controlador
        controladorUsuario = new ControladorUsuario();
        //crear el usuario
        usuario = new Usuario("11111111A", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123", "sandra@gmail.com", "1234");
        //y meter al usuario para que haya uno al menos
        controladorUsuario.registrarUsuario(usuario);
    }

    //cuidado, no repetir email
    @Test
    void registrarUsuarioCorrecto() {
        //creamos un usuario con todo correcto y lo registramos
        Usuario usuarioCorrecto = new Usuario("11111111B", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123", "sandra2@gmail.com", "1234");
        controladorUsuario.registrarUsuario(usuarioCorrecto);

        //nos tiene que salir con size 2 (lo que viene a ser lo mismo que la id) ya que el 1 lo metemos con el BerforeEach
        assertEquals(2, controladorUsuario.leerUsuarios().size());
    }

    //repetir email
    @Test
    void registrarUsuarioIncorrecto() {
        //creamos un usuario con todo correcto y lo registramos
        Usuario usuarioIncorrecto = new Usuario("11111111B", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123", "sandra@gmail.com", "1234");
        //poner el registrarUsuario dentro del assert, no fuera que da error

        //nos tiene que salir con size 2 (lo que viene a ser lo mismo que la id) ya que el 1 lo metemos con el BerforeEach
        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.registrarUsuario(usuarioIncorrecto);
        });
    }

    @Test
    void loginCorrecto() {
        //guardar en un usuario aparte para poder comparar después
        Usuario loginCorrecto = controladorUsuario.login(usuario.getEmail(), usuario.getPasssword());

        assertEquals(usuario, loginCorrecto);
    }

    @Test
    void loginIncorrecto() {
        //probar email incorrecto
        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.login("gdfgdgh", "1234");
        });

        //probar passwrod incorrecta
        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.login("sandra@gmail.com", "sgdrgrg");
        });
    }

    @Test
    void leerUsuarioPorIdCorrecto() {
        Usuario userPorId = controladorUsuario.leerUsuarioPorId(usuario.getId());

        assertEquals(usuario, userPorId);
    }

    @Test
    void leerUsuarioPorIdIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.leerUsuarioPorId(5);
        });
    }

    @Test
    void eliminarUsuarioCorrecto() {
        //quitamos el unico usuario que hay
        controladorUsuario.eliminarUsuario(usuario.getId());

        //comprobamos que hay 0 ahora
        assertEquals(0, controladorUsuario.leerUsuarios().size());
    }

    @Test
    void eliminarUsuarioIncorrecto() {
        //creamos un usuario y le asignamos manualmente una id q no esté en la lista para que de error
        Usuario noEsta = new Usuario("11111111A", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123", "sandra2@gmail.com", "1234");
        noEsta.setId(1234);

        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.eliminarUsuario(noEsta.getId());
        });
    }

    //test comprobar actualizar
    @Test
    void actualizarUsuarioCorrecto() {
        Usuario modificado = new Usuario("11111111A", "Calle Original",
                LocalDate.of(2005, 9, 12), "11111111", "sandra2@gmail.com", "1234");
        //le ponemos el mismo id del anterior
        modificado.setId(1);
        //actualizamos
        controladorUsuario.actualizarUsuario(modificado);
        Usuario actualizado = controladorUsuario.leerUsuarioPorId(1);

        assertEquals("11111111", actualizado.getTelefono());
    }

    @Test
    void actualizarUsuarioInexistente() {
        Usuario inexistente = new Usuario("11111111A", "Calle Original",
                LocalDate.of(2005, 9, 12), "11111111", "sandra2@gmail.com", "1234");
        //le ponemos id inexistente
        inexistente.setId(11111);

        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.actualizarUsuario(inexistente);
        });
    }

    @Test
    void actualizarUsuarioCampoIneditable() {
        Usuario modificado = new Usuario("11111111A", "Calle Original",
                LocalDate.of(2005, 9, 12), "11111111", "sandra2@gmail.com", "1234");
        //le ponemos id igual al anterior
        modificado.setId(1);
        //cambiamos DNI que recordemos que NO SE PUEDE
        modificado.setDni("99999999Z");

        assertThrows(IllegalArgumentException.class, () -> {
            controladorUsuario.actualizarUsuario(modificado);
        });
    }

    //comprobar que asigna bien los id
    @Test
    void comprobarIdUsuariosRegistrados() {
        //vemos la id del usuario metido
        long nuevoId = usuario.getId();

        assertEquals(1, nuevoId);
    }

    @Test
    void aniadirProductoFavoritoCorrecto() {
        //creamos lo necesario
        etiqueta = new Etiqueta("Nuevo");
        camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
        //añadimos el producto a favorito
        controladorUsuario.aniadirProductoFavorito(camisa, usuario);

        //miramos si el Set contiene el producto
        assertTrue(usuario.getFavoritos().contains(camisa));
    }

    @Test
    void aniadirProductoFavoritoDuplicado() {
        //creamos lo necesario
        etiqueta = new Etiqueta("Nuevo");
        camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
        controladorUsuario.aniadirProductoFavorito(camisa, usuario);
        controladorUsuario.aniadirProductoFavorito(camisa, usuario);

        //al ser Set no duplica
        assertEquals(1, usuario.getFavoritos().size());
    }

    @Test
    void eliminarProductoFavorito() {
        //creamos lo necesario
        etiqueta = new Etiqueta("Nuevo");
        camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
        //lo añadimos y eliminamos
        controladorUsuario.aniadirProductoFavorito(camisa, usuario);
        controladorUsuario.eliminarProductoFavorito(camisa, usuario);

        //comprobamos que no lo contiene
        assertFalse(usuario.getFavoritos().contains(camisa));

    }

    @Test
    void eliminarProductoFavoritoNoExistenteNoFalla() {
        //creamos lo necesario
        etiqueta = new Etiqueta("Nuevo");
        camisa = new Camisa("Camisa Casual", "MarcaX", 29.99, Talla.M, Color.AZUL, etiqueta, 2);
        // eliminar antes de haberlo añadido
        controladorUsuario.eliminarProductoFavorito(camisa, usuario);

        //comprobamos que no lo contiene
        assertFalse(usuario.getFavoritos().contains(camisa));
    }


}
