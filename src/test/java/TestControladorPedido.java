
import org.example.controller.ControladorPedido;
import org.example.model.Usuario;
import org.example.model.descuento.DescuentoFijo;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Chaqueta;
import org.example.model.producto.tipo_de_productos.Pantalon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
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
        //comprobación
        assertEquals(usuario, pedido.getUsuario());
        //también comprobamos que se cree el pedido pendiente
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    }

    //test para borrar un pedido correcto (error), comprobamos que no encuentra el pedido por lo que lanza exception
    @Test
    void borrarPedidoCorrecto() {
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

    //test para el buen funcionamiento de actualizar
    @Test
    void actualizarPedidoCambiaEstadoCorrecto() {
        // Crear un pedido inicial
        Pedido pedido = controladorPedido.crearPedido(usuario);
        // Crear un pedido con el mismo ID pero estado distinto
        Pedido pedidoActualizado = new Pedido(usuario);
        pedidoActualizado.setId(pedido.getId());  // mismo ID
        // Cambiamos el estado a uno distinto del original
        pedidoActualizado.setEstado(EstadoPedido.ENTREGADO);

        // Copiamos el resto de campos igual al pedido original para no alterar restricciones
        pedidoActualizado.setFecha(pedido.getFecha());
        pedidoActualizado.setUsuario(pedido.getUsuario());

        // Llamamos al metodo y esperamos que no lance excepción
        Pedido pedidoResultante = controladorPedido.actualizarDatosPedido(pedidoActualizado);

        // Comprobamos que el estado sí se ha actualizado
        assertEquals(EstadoPedido.ENTREGADO, pedidoResultante.getEstado());

        // Comprobamos que el ID no ha cambiado
        assertEquals(pedido.getId(), pedidoResultante.getId());
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
        controladorPedido.crearPedido(usuario);
        //finalizamos el pedido
        Pedido finalizado = controladorPedido.finalizarPedido(usuario);
        //comprobamos que ha finalizado bien
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
        controladorPedido.crearPedido(usuario);
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
        controladorPedido.crearPedido(usuario);
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
        assertThrows(IllegalStateException.class, () -> controladorPedido.entregarPedido(pedido.getId()));
    }

    //TEST CRUD LINEAPEDIDO
    //test para añadir linea de pedido si no existe pedido anteriormente
    @Test
    void aniadirLineaPedidoPrimeraVezCrearPedidoCorrecto() {
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

    //test para añadir línea de pedido ya existiendo un pedido pendiente
    @Test
    void aniadirLineaPedidoSegundaVezCorrecto() {
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
        Set<LineaPedido> lineas = controladorPedido.leerLineasPedidoDePedidoConcreto(usuario);
        assertEquals(1, lineas.size());
    }

    //test leer líneas incorrecto ya que no hay líneas añadidas (error)
    @Test
    void leerLineasPedidoSinPedidoPendienteIncorrecto() {
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.leerLineasPedidoDePedidoConcreto(usuario));
    }

    //test actualizar una línea de pedido correctamente
    @Test
    void actualizarLineaPedidoCorrecto() {
        //creamos el pedido y la línea
        Pedido pedido = controladorPedido.crearPedido(usuario);
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);

        //creamos una copia de este con diferente cantidad para actualizarla
        LineaPedido copia = new LineaPedido(5, producto, pedido);
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
        Pedido pedido = controladorPedido.crearPedido(usuario);
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);
        //creamos un producto distinto
        Producto otroProducto = new Chaqueta("Chaquetón", "Adidas", 45, Talla.L, Color.NEGRO, etiqueta, true, 3);
        //creamos una copia de la línea pero cambiando el producto
        LineaPedido copia = new LineaPedido(3, otroProducto, pedido);
        //le ponemos el id del original
        copia.setId(linea.getId());
        //comprobamos que no se puede cambiar el producto
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.actualizarLineasPedidoDePedidoConcreto(usuario, copia));
    }

    //test eliminar una línea de pedido
    @Test
    void eliminarLineaPedidoCorrecto() {
        controladorPedido.crearPedido(usuario);
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);
        //eliminar la línea
        controladorPedido.eliminarLineaPedidoDePedido(usuario, linea.getId());
        //comprobamos que no hay líneas
        assertEquals(0, controladorPedido.leerLineasPedidoDePedidoConcreto(usuario).size());
        assertEquals(null, producto.getLineaPedido());
    }

    //test para eliminar una línea de pedido inexistente (error)
    @Test
    void eliminarLineaPedidoIncorrecto() {
        controladorPedido.crearPedido(usuario);
        assertThrows(IllegalArgumentException.class, () -> controladorPedido.eliminarLineaPedidoDePedido(usuario, 999));
    }


    //TEST COMPROBAR PRECIOS
    //test para probar metodo getPrecioSubtotal
    @Test
    void lineaPedidoSubtotalCorrecto() {
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 4);
        assertEquals(100.0, linea.getPrecioSubTotal());
    }

    //test para probar descuento porcentaje
    @Test
    void productoConDescuentoPorcentaje() {
        Producto pantalon = new Pantalon("Pantalón Vaquero", "Levis", 100, Talla.M, Color.AZUL, etiqueta, 3);
        //10% descuento
        pantalon.setDescuento(new DescuentoPorcentaje(10));
        //añadir la línea (con el descuento)
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, pantalon, 1);

        // 100 - 10% = 90
        assertEquals(90.0, linea.getProducto().getPrecioFinal());
        assertEquals(90.0, linea.getPrecioSubTotal());
    }

    //test para probar descuento porcentaje
    @Test
    void productoConDescuentoFijo() {
        Producto chaqueta = new Chaqueta("Chaqueta Premium", "Gucci", 200, Talla.L, Color.NEGRO, etiqueta, true, 5);
        //50 euros de descuento
        chaqueta.setDescuento(new DescuentoFijo(50));
        LineaPedido linea = controladorPedido.aniadirLineaPedidoAPedido(usuario, chaqueta, 1);

        // 200 - 50 = 150
        assertEquals(150.0, linea.getProducto().getPrecioFinal());
        assertEquals(150.0, linea.getPrecioSubTotal());
    }

    //test para comprobar el metodo getPrecioTotal
    @Test
    void pedidoPrecioTotalConDescuentoFijo() {
        Producto chaqueta = new Chaqueta("Chaqueta Premium", "Gucci", 200, Talla.L, Color.NEGRO, etiqueta, true, 5);
        chaqueta.setDescuento(new DescuentoFijo(30)); // 200 - 30 = 170
        controladorPedido.aniadirLineaPedidoAPedido(usuario, chaqueta, 1);
        Pedido pedido = controladorPedido.encontrarPedidoPendienteDeUsuarioConcreto(usuario);

        assertEquals(170.0, pedido.getPecioTotal());
    }

    //TEST EXPORTAR E IMPORTAR PEDIDOS GSON
    @Test
    void exportarPedidosGsonCorrecto() throws IOException {
        //crear pedido y añadir una línea para poder visualizarla
        Pedido pedido = controladorPedido.crearPedido(usuario);
        controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 2);
        //exportar
        controladorPedido.exportarPedidosGson();
        //comprobar que se ha creado el archivo, leer el contenido y verificar que sale lo que tiene que salir
        File file = new File("pedidos.json");
        assertTrue(file.exists());
        String contenido = Files.readString(file.toPath());
        System.out.println(contenido);
        assertTrue(contenido.contains("\"estado\": \"PENDIENTE\""),
                "El JSON debe contener el estado PENDIENTE del pedido");
        assertTrue(contenido.contains("\"cantidad\": 2"),
                "El JSON debe incluir la cantidad de la línea de pedido");
    }

    //NO PROBAR EXPORTAR INCORRECTAMENTE GSON YA QUE EN EL PROPIO METODO SE PONE LA RUTA DEL ARCHIVO POR LO QUE NO VA A FALLAR NUNCA

    @Test
    void importarPedidosGsonCorrecto() {
        //crear pedido y exportar
        Pedido pedido = controladorPedido.crearPedido(usuario);
        controladorPedido.aniadirLineaPedidoAPedido(usuario, producto, 1);
        controladorPedido.exportarPedidosGson();

        //vaciar lista y luego importar
        controladorPedido.leerPedidos().clear();
        assertEquals(0, controladorPedido.leerPedidos().size(), "La lista debe estar vacía antes de importar");
        controladorPedido.importarPedidosGson();

        //comprobar que funciona
        List<Pedido> pedidosImportados = controladorPedido.leerPedidos();
        assertEquals(1, pedidosImportados.size(), "Debe importar un pedido");
    }

    @Test
    void importarPedidosGsonIncorrectoArchivoNoExiste() {
        //borrar el fichero por si existe (para que de error)
        File file = new File("pedidos.json");
        if (file.exists()) file.delete();

        //comprobamos que falla
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                controladorPedido.importarPedidosGson()
        );

        assertTrue(exception.getMessage().contains("Error al importar pedidos desde JSON"),
                "Debe lanzar un mensaje de error adecuado al fallar la importación");
    }


}
