
import org.example.controller.ControladorPedido;
import org.example.model.Usuario;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipoDeProductos.Camisa;
import org.example.model.producto.tipoDeProductos.Chaqueta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TestControladorPedido {
    private ControladorPedido controladorPedido;
    private Usuario usuario;
    private Producto producto;
    private Etiqueta etiqueta;

    //creamos los objetos comunes necesarios para probar los metodos
    @BeforeEach
    void setUp() {
        controladorPedido = new ControladorPedido();
        usuario = new Usuario("11111111A", "Alguna calle",
                LocalDate.of(2005, 9, 12), "600123123", "sandra@gmail.com", "1234");
        producto = new Camisa("Camisa Blanca", "Nike", 25, Talla.L, Color.BLANCO, etiqueta, 0);
    }

    //TEST CRUD DE PEDIDO
    //test para crear un pedido, comprobamos que se ha asignado bien el usuario y que tiene el estado del pedido como pendiente
    @Test
    void crearPedido() {
        //Creamos un pedido con el metodo
        Pedido pedido = controladorPedido.crearPedido(usuario);
        //comprobacion
        assertEquals(usuario, pedido.getUsuario());
        //también comprobamos que se cree el pedido pendiente
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    //test para borrar un pedido, comprobamos que no encuentra el pedido por lo que lanza exception
    @Test
    void testBorrarPedidoCorrecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        controladorPedido.borrarPedido(pedido.getId());
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.leerPedidoPorId(pedido.getId()));
    }

    //test para ver si se asignan bien los ids y se puede encontrar un pedido con este metodo
    @Test
    void leerPedidoPorIdCorrecto() {
        //creamos el pedido
        Pedido pedido = controladorPedido.crearPedido(usuario);
        //leemos el pedido por la id del creado anteriormente
        Pedido pedidoEncontrado = controladorPedido.leerPedidoPorId(pedido.getId());
        assertEquals(pedido, pedidoEncontrado);
    }

    //comprobamos el caso de que falle leer un pedido por id ya que no existe ese id
    @Test
    void leerPedidoPorIdIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.leerPedidoPorId(1000));
    }


    //test para actualizar un pedido (realmente no se puede actualizar nada)
    @Test
    void actualizarPedidoCambiaIdIncorrecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        Pedido pedidoActualizado = new Pedido(usuario);
        pedidoActualizado.setId(1000);
        pedidoActualizado.setEstado(pedido.getEstado());
        //le pasamos el nuevo pedido actualizado
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.actualizarDatosPedido(pedidoActualizado));
    }



    //TEST DE CAMBIAR ESTADO
    //test para cer si podemos finalizar un pedido sin problema
    @Test
    void finalizarPedidoCorrecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        Pedido finalizado = controladorPedido.finalizarPedido(usuario);
        assertEquals(EstadoPedido.FINALIZADO, finalizado.getEstado());
    }

    //test para finalizar un pedido incorrecto ya que no hay ningún pedido pendiente creado (error)
    @Test
    void finalizarPedidoSinPendienteIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.finalizarPedido(usuario));
    }

    //test para poder cancelar un pedido
    @Test
    void cancelarPedidoCorrecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        Pedido pedidoCancelado = controladorPedido.cancelarPedido(usuario);
        assertEquals(EstadoPedido.CANCELADO, pedidoCancelado.getEstado());
    }

    //cancelar pedido sin que exista un pedido pendiente de un usuario (error)
    @Test
    void cancelarPedidoSinPendienteIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.cancelarPedido(usuario));
    }

    //test cancelar un pedido ya finalizado (error)
    @Test
    void cancelarPedidoFinalizadoIncorecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        controladorPedido.finalizarPedido(usuario);
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.cancelarPedido(usuario));
    }

    //test para entregar un pedido
    @Test
    void entregarPedidoCorrecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        controladorPedido.finalizarPedido(usuario);
        Pedido entregado = controladorPedido.entregarPedido(pedido.getId());
        assertEquals(EstadoPedido.ENTREGADO, entregado.getEstado());
    }

    //test para cancelar un pedido el cual no ha sido finalizado (error)
    @Test
    void entregarPedidoNoFinalizadoIncorrecto() {
        Pedido pedido = controladorPedido.crearPedido(usuario);
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.entregarPedido(pedido.getId()));
    }

    //TEST CRUD LINEAPEDIDO
    //test para añadir linea de pedido si no existe pedido anteriormente
    @Test
    void añadirLineaPedidoPrimeraVezCrearPedidoCorrecto() {
        //creamos la línea
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 5);
        //comprobamos que se ha creado bien
        assertEquals(5, linea.getCantidad());
        assertEquals(producto, linea.getProducto());

        // se ha creado automáticamente un pedido pendiente
        Pedido pedido = usuario.getPedidos().iterator().next();
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(1, pedido.getLineasPedido().size());
    }

    //me salta la excepcion de que no encuentra usuario con pedido pendiente en los dos primeros test

    //test para añadir línea de pedido ya existiendo un pedido pendiente
    @Test
    void añadirLineaPedidoSegundaVezCorrecto() {
        //añadimos una línea de pedido
        controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 1);

        //creamos otro producto
        Producto producto2 = new Chaqueta("Chaquetón", "Adidas", 45, Talla.L, Color.NEGRO, etiqueta, true, 3);
        //añadimos la línea al pedido
        LineaPedido segundaLinea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto2, 3);

        // debe seguir en el mismo pedido pendiente
        Pedido pedido = usuario.getPedidos().iterator().next();
        assertEquals(2, pedido.getLineasPedido().size());
        assertEquals(3, segundaLinea.getCantidad());
        assertEquals(producto2, segundaLinea.getProducto());
    }

    //test para añadir una línea de pedido incorrecto NO SE HACE, ya que nunca da exception

    //test leer líneas de pedido bien
    //guardar en un set las líneas y comprobar el size después
    @Test
    void leerLineasPedidoCorrecto() {
        controladorPedido.crearPedido(usuario);
        controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);
        Set<LineaPedido> lineas = controladorPedido.leerLineasPedidoDeDedidoConcreto(usuario);
        assertEquals(1, lineas.size());
    }

    //test leer líneas incorrecto ya que no hay líneas añadidas (error)
    @Test
    void leerLineasPedidoSinPedidoPendienteIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.leerLineasPedidoDeDedidoConcreto(usuario));
    }

    //test actualizar una línea de pedido correctamente
    @Test
    void actualizarLineaPedidoCorrecto() {
        //creamos el pedido y la línea
        controladorPedido.crearPedido(usuario);
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);

        //creamos una copia de este con diferente cantidad para actualizarla
        LineaPedido copia = new LineaPedido(5, producto);
        //le ponemos el mismo id que al anterior
        copia.setId(linea.getId());

        //pasamos la línea modificada al metodo de actualizar
        Set<LineaPedido> actualizadas = controladorPedido.actualizarLineasPedidoDePedidoConcreto(usuario, copia);
        //comprobamos que está la cantidad cambiada
        assertEquals(5, actualizadas.iterator().next().getCantidad());
    }

    //test para actualizar el producto de una línea de pedido (error)
    @Test
    void actualizarLineaPedidoCambiaProductoIncorrecto() {
        controladorPedido.crearPedido(usuario);
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);
        //creamos un producto distinto
        Producto otroProducto = new Chaqueta("Chaquetón", "Adidas", 45, Talla.L, Color.NEGRO, etiqueta, true, 3);
        //creamos una copia de la línea pero cambiando el producto
        LineaPedido copia = new LineaPedido(3, otroProducto);
        //le ponemos el id del original
        copia.setId(linea.getId());
        //comprobamos que no se puede cambiar el producto
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.actualizarLineasPedidoDePedidoConcreto(usuario, copia));
    }

    //test eliminar una línea de pedido
    @Test
    void testEliminarLineaPedidoCorrecto() {
        controladorPedido.crearPedido(usuario);
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);
        //eliminar la línea
        controladorPedido.eliminarLineaPedidoDePedido(usuario, linea.getId());
        //comprobamos que no hay líneas
        assertEquals(0, controladorPedido.leerLineasPedidoDeDedidoConcreto(usuario).size());
        assertEquals(null, producto.getLineaPedido());
    }

    //test para eliminar una línea de pedido inexistente (error)
    @Test
    void testEliminarLineaPedidoIncorrecto() {
        controladorPedido.crearPedido(usuario);
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.eliminarLineaPedidoDePedido(usuario, 999));
    }


}
