
import org.example.controller.ControladorProducto;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestControladorProducto {
    private ControladorProducto controladorProducto;
    private Etiqueta etiqueta;
    private Producto p;

    @BeforeEach
    void setUp() {
        controladorProducto = new ControladorProducto();
        etiqueta = new Etiqueta("Rebajas");
    }

    //TEST CRUD DE PRODUCTO
    //test crear una camisa (y ver que se añade a la lista) no repito con pantalon y chaqueta ya que el codigo es igual
    @Test
    void crearCamisaCorrecto() {
        p = controladorProducto.crearCamisa("Camisa Blanca", "Nike", 25,
                Talla.M, Color.BLANCO, etiqueta, 3);

        assertEquals(1, controladorProducto.leerProductos().size());
    }

    //test incorrecto (error) al crear una camisa (no hago todas las pruebas ya que es siempre lo mismo)
    @Test
    void crearCamisaNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                controladorProducto.crearCamisa(null, "Nike", 25, Talla.M, Color.BLANCO, etiqueta, 3));
    }

    //test para probar que devuelve la lista de productos
    @Test
    void leerProductos() {
        controladorProducto.crearCamisa("Camisa", "Nike", 25, Talla.M, Color.BLANCO, etiqueta, 3);

        assertEquals(1, controladorProducto.leerProductos().size());
    }

    //test para buscar un producto por id correcto
    @Test
    void buscarProductoPorIdCorrecto() {
        p = controladorProducto.crearCamisa("Camisa", "Nike", 25, Talla.M, Color.BLANCO, etiqueta, 3);
        Producto encontrado = controladorProducto.buscarProductoPorId(p.getId());

        assertEquals(p, encontrado);
    }

    //test para buscar un producto por id incorrecto (error)
    @Test
    void buscarProductoPorIdInexistente() {
        assertThrows(IllegalArgumentException.class, () -> controladorProducto.buscarProductoPorId(999));
    }

    //test borrar un producto correcto
    @Test
    void eliminarProductoCorrecto() {
        p = controladorProducto.crearCamisa("Camisa Roja", "Puma", 40,
                Talla.S, Color.ROJO, etiqueta, 4);
        //comprobamos que se añadió a la lista
        assertEquals(1, controladorProducto.leerProductos().size());
        //borrar el producto
        controladorProducto.eliminarUsuario(p.getId());

        //comprobamos que la lista está vacía
        assertEquals(0, controladorProducto.leerProductos().size());
    }

    //test para borrar un producto incorrecto (error) (no encuentra la id)
    @Test
    void eliminarProductoIdInexistente() {
        assertThrows(IllegalArgumentException.class, () -> controladorProducto.eliminarUsuario(999));
    }

    //test actualizar un producto correcto (sin intentar cambiar nada que no se puede)
    @Test
    void actualizarProductoCorrecto() {
        p = controladorProducto.crearCamisa("Camisa", "Nike", 25, Talla.M, Color.BLANCO, etiqueta, 3);
        //creamos uno con los datos nuevos
        Producto productoNuevo = new Camisa("Camisa Azul", "Adidas", 30, Talla.L, Color.AZUL, etiqueta, 4);
        //le ponemos la id del viejo
        productoNuevo.setId(p.getId());
        //dejamos las líneas y usuariosProductosFavoritos del viejo ya que no se pueden cambiar estos datos
        productoNuevo.setLineaPedido(p.getLineaPedido());
        productoNuevo.setUsuariosProductosFavoritos(p.getUsuariosProductosFavoritos());
        //actualizar
        Producto actualizado = controladorProducto.actualizarProducto(productoNuevo);

        assertEquals("Camisa Azul", actualizado.getNombre());
        assertEquals("Adidas", actualizado.getMarca());
        assertEquals(30, actualizado.getPrecioInicial());
    }

    //test incorrecto (error) de actualizar algo que no se puede cambiar (no pruebo todos)
    @Test
    void actualizarProductoCambiaIdIncorrecto() {
        p = controladorProducto.crearCamisa("Camisa", "Nike", 25, Talla.M, Color.BLANCO, etiqueta, 3);
        Producto productoNuevo = new Camisa("Camisa Azul", "Adidas", 30, Talla.L, Color.AZUL, etiqueta, 4);
        //cambiar id
        productoNuevo.setId(p.getId() + 1);

        assertThrows(IllegalArgumentException.class, () -> controladorProducto.actualizarProducto(productoNuevo));
    }

    //TEST DE DESCUENTOS
    //test donde probamos a poner un descuento y después quitarlo
    //la comprobación de los precios con y sin descuento están en test de pedido
    @Test
    void asignarYEliminarDescuentoCorrecto() {
        p = controladorProducto.crearCamisa("Camisa", "Nike", 25, Talla.M, Color.BLANCO, etiqueta, 3);
        Producto d = new DescuentoPorcentaje(0.2f); // 20%
        controladorProducto.asignarDescuento(p.getId(), d);

        assertEquals(d, p.getDescuento());

        controladorProducto.eliminarDescuento(p.getId());
    }


}
