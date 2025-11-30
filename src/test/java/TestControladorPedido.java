
import org.example.controller.ControladorPedido;
import org.example.controller.ControladorProducto;
import org.example.controller.ControladorUsuario;
import org.example.model.Usuario;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Pantalon;
import org.example.utils.DatabaseConf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestControladorPedido {
    private ControladorPedido controladorPedido;
    private Usuario usuario;

    private ControladorUsuario controladorUsuario;
    private ControladorProducto controladorProducto;

    private Producto camisa;
    private Pedido pedido;
    private LineaPedido linea;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConf.dropAndCreateTables();

        controladorPedido = new ControladorPedido();
        controladorUsuario = new ControladorUsuario();
        controladorProducto = new ControladorProducto();

        // Usuario
        usuario = new Usuario(
                "Sandra", "11111111A", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123",
                "sandra@gmail.com", "1234"
        );
        controladorUsuario.registrarUsuario(usuario);

        // Producto
        Etiqueta etiqueta = new Etiqueta("NOVEDAD");
        camisa = new Camisa(
                "Camisa Azul", "Zara", 29.99,
                Talla.M, Color.AZUL, etiqueta, 5
        );
        controladorProducto.crearProducto(camisa);

        // Pedido sin ID todavía
        pedido = new Pedido(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Línea de pedido sin ID tampoco
        linea = new LineaPedido(2, camisa, pedido);
        pedido.getLineasPedido().add(linea);
    }

    // no comentada porque es prácticamente igual que los otros test

    // CRUD
    // SAVE
    @Test
    void crearPedidoCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        List<Pedido> pedidos = controladorPedido.obtenerTodos();
        assertEquals(1, pedidos.size());
        assertEquals(usuario.getId(), pedidos.get(0).getUsuario().getId());
    }

    @Test
    void crearPedidoIncorrecto() {
        // pedido con usuario null
        Pedido pInvalido = new Pedido(null);

        assertThrows(SQLException.class, () -> controladorPedido.crearPedido(pInvalido));
    }

    // FIND BY ID
    @Test
    void buscarPorIdCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        Optional<Pedido> encontrado = controladorPedido.buscarPorId(pedido.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(EstadoPedido.PENDIENTE, encontrado.get().getEstado());
    }

    @Test
    void buscarPorIdIncorrecto() throws SQLException {
        Optional<Pedido> encontrado = controladorPedido.buscarPorId(99999);
        assertTrue(encontrado.isEmpty());
    }

    // FIND ALL
    @Test
    void obtenerTodosCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        assertEquals(1, controladorPedido.obtenerTodos().size());
    }

    // UPDATE
    @Test
    void actualizarPedidoCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        pedido.setEstado(EstadoPedido.FINALIZADO);
        controladorPedido.actualizarPedido(pedido);

        Pedido actualizado = controladorPedido.buscarPorId(pedido.getId()).get();
        assertEquals(EstadoPedido.FINALIZADO, actualizado.getEstado());
    }

    @Test
    void actualizarPedidoIncorrecto() {
        Pedido p = new Pedido(usuario);
        p.setId(99999); // no existe en BD
        p.setEstado(EstadoPedido.CANCELADO);

        assertThrows(SQLException.class, () -> controladorPedido.actualizarPedido(p));
    }

    // DELETE
    @Test
    void eliminarPedidoCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        controladorPedido.eliminarPedido(pedido.getId());

        assertEquals(0, controladorPedido.obtenerTodos().size());
    }

    @Test
    void eliminarPedidoIncorrecto() {
        assertThrows(SQLException.class, () -> controladorPedido.eliminarPedido(987654));
    }

    // METODOS ESPECÍFICOS
    // FIND BY CLIENTE
    @Test
    void pedidosPorClienteCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        List<Pedido> pedidos = controladorPedido.pedidosPorCliente(usuario.getId());

        assertEquals(1, pedidos.size());
        assertEquals(usuario.getId(), pedidos.get(0).getUsuario().getId());
    }

    @Test
    void pedidosPorClienteIncorrecto() {
        assertThrows(SQLException.class,
                () -> controladorPedido.pedidosPorCliente(999999));
    }

    // FIND BY ESTADO
    @Test
    void pedidosPorEstadoCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        List<Pedido> pendientes = controladorPedido.pedidosPorEstado(EstadoPedido.PENDIENTE);

        assertEquals(1, pendientes.size());
    }

    @Test
    void pedidosPorEstadoIncorrecto() {
        assertThrows(NullPointerException.class,
                () -> controladorPedido.pedidosPorEstado(null));
    }

    // FIND LINEAS BY PEDIDO
    @Test
    void obtenerLineasDePedidoCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        List<LineaPedido> lineas = controladorPedido.lineasDePedido(pedido.getId());

        assertEquals(1, lineas.size());
        assertEquals(camisa.getId(), lineas.get(0).getProducto().getId());
    }

    @Test
    void obtenerLineasDePedidoIncorrecto() {
        assertThrows(SQLException.class,
                () -> controladorPedido.lineasDePedido(888888));
    }

    // ADD LINEA PEDIDO
    @Test
    void agregarLineaPedidoCorrecto() throws SQLException {
        controladorPedido.crearPedido(pedido);

        LineaPedido nueva = new LineaPedido(1, camisa, pedido);
        controladorPedido.agregarLineaPedido(nueva);

        List<LineaPedido> lineas = controladorPedido.lineasDePedido(pedido.getId());
        assertEquals(2, lineas.size());
    }

    @Test
    void agregarLineaPedidoPedidoNoExistenteIncorrecto() {
        Pedido pFalso = new Pedido(usuario);
        pFalso.setId(99999);

        LineaPedido lp = new LineaPedido(1, camisa, pFalso);

        assertThrows(SQLException.class, () -> controladorPedido.agregarLineaPedido(lp));
    }

    // FUNCIONALIDADES
    // CATÁLOGO DE PRODUCTOS
    @Test
    void crearYListarProductos() throws SQLException {
        // creamos pantalon
        Producto pantalon = new Pantalon(
                "Camisa Azul", "Zara", 29.99,
                Talla.M, Color.AZUL, null, 5
        );
        // añadimos el pantalon (podrian ser varios productos)
        controladorProducto.crearProducto(pantalon);

        // obtenemos todos (simular que se cargan todos y que el usuario pueda verlos)
        List<Producto> productos = controladorProducto.obtenerTodos();
        // comprobamos que se ha obtenido el pantalon
        assertTrue(productos.contains(pantalon));
    }

    // FINALIZAR PEDIDO
    @Test
    void finalizarPedidoPendiente() throws SQLException {
        // creamos el pedido en la base (antes no se hacía)
        controladorPedido.crearPedido(pedido);
        // lo finalizamos
        controladorPedido.finalizarPedido(pedido.getUsuario(), "TARJETA");

        // buscamos el pedido y comprobamos si lo encuentra y si el estado es finalizado
        Optional<Pedido> actualizado = controladorPedido.buscarPorId(pedido.getId());
        assertTrue(actualizado.isPresent());
        assertEquals(EstadoPedido.FINALIZADO, actualizado.get().getEstado());
    }

    @Test
    void finalizarPedidoPendienteSinPedidoIncorrecto() {
        // crear otro usuario sin pedido
        Usuario usuario2 = new Usuario(
                "Luis", "22222222B", "Otra calle",
                LocalDate.of(2000, 1, 1), "600000000",
                "luis@gmail.com", "abcd"
        );

        // registramos
        assertDoesNotThrow(() -> controladorUsuario.registrarUsuario(usuario2));
        // comprobamos que da exception al finalizarle un pedido
        SQLException exception = assertThrows(SQLException.class,
                () -> controladorPedido.finalizarPedido(usuario2, "TARJETA"));
        assertEquals("No hay pedido pendiente para el usuario con id " + usuario2.getId(), exception.getMessage());
    }

    // CANCELAR PEDIDO
    @Test
    void cancelarPedidoPendiente() throws SQLException {
        // misma dinámica que el finalizar
        controladorPedido.crearPedido(pedido);
        controladorPedido.cancelarPedido(usuario);

        Optional<Pedido> actualizado = controladorPedido.buscarPorId(pedido.getId());
        assertTrue(actualizado.isPresent());
        assertEquals(EstadoPedido.CANCELADO, actualizado.get().getEstado());
    }

    // ENTREGAR PEDIDO
    @Test
    void entregarPedido() throws SQLException {
        // misma dinámica que finalizar
        controladorPedido.crearPedido(pedido);
        // primero finalizamos para simular flujo normal
        controladorPedido.finalizarPedido(usuario, "TARJETA");
        controladorPedido.entregarPedido(pedido.getId());

        Optional<Pedido> actualizado = controladorPedido.buscarPorId(pedido.getId());
        assertTrue(actualizado.isPresent());
        assertEquals(EstadoPedido.ENTREGADO, actualizado.get().getEstado());
    }
}
