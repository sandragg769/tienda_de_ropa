
import org.example.controller.ControladorProducto;
import org.example.model.descuento.DescuentoPorcentaje;
import org.example.model.producto.Etiqueta;
import org.example.model.producto.Producto;
import org.example.model.producto.enumeraciones.Color;
import org.example.model.producto.enumeraciones.Talla;
import org.example.model.producto.tipo_de_productos.Camisa;
import org.example.model.producto.tipo_de_productos.Pantalon;
import org.example.utils.GestorFicherosJSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        controladorProducto.eliminarProducto(p.getId());

        //comprobamos que la lista está vacía
        assertEquals(0, controladorProducto.leerProductos().size());
    }

    //test para borrar un producto incorrecto (error) (no encuentra la id)
    @Test
    void eliminarProductoIdInexistente() {
        assertThrows(IllegalArgumentException.class, () -> controladorProducto.eliminarProducto(999));
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
        //creamos un descuento porcentaje de 20%
        DescuentoPorcentaje d = new DescuentoPorcentaje(0.2f);
        //le asignamos el descuento
        controladorProducto.asignarDescuento(p.getId(), d);
        //comprobamos que lo tiene
        assertEquals(d, p.getDescuento());
        //lo eliminamos
        controladorProducto.eliminarDescuento(p.getId());
        //comprobamos que no lo tiene ahora
        assertEquals(null, p.getDescuento());
    }

    //TEST EXPORTAR E IMPORTAR JSON
    //test exportar JSON correcto y verificando el tipo
    @Test
    void exportarProductosCorrecto() throws IOException {
        //aunque se tenga que crear el objeto en estos test siempre mejor no poner en el BeforeEach para que no interfiera con los test que ya tengo
        Producto camisa = new Camisa();
        camisa.setNombre("Camisa Formal");
        camisa.setPrecioInicial(25.0);
        camisa.setEtiqueta(etiqueta);
        controladorProducto.leerProductos().add(camisa);

        //exportar
        controladorProducto.exportarProductosAJSON();

        //crea un file apuntando al fichero donde se exporta
        File file = new File("productos.json");
        //leer contenido
        String contenido = Files.readString(file.toPath());
        //comprobar que contiene el campo tipo con el nombre "Camisa"
        assertTrue(contenido.contains("\"tipo\":\"Camisa\""), "El JSON debe incluir el campo tipo con valor Camisa");
    }

    //test ruta invalida de exportar a JSON
    @Test
    void exportarProductosIncorrectoRutaInvalida() {
        Producto pantalon = new Pantalon();
        pantalon.setNombre("ProductoTest");
        pantalon.setPrecioInicial(20.0);
        pantalon.setEtiqueta(etiqueta);
        controladorProducto.leerProductos().add(pantalon);

        //comprobar que da error al poner una ruta inválida
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            GestorFicherosJSON.exportarProductosAJSON(controladorProducto.leerProductos(), "/ruta/invalida/productos.json");
        });
        System.out.println("Mensaje excepción: " + exception.getMessage());
        assertTrue(exception.getMessage().startsWith("Error al exportar productos a JSON"), "Mensaje de error esperado");
    }

    //test correcto de importar desde JSON
    @Test
    void importarProductosCorrecto() {
        //creamos el objeto y lo exportamos primero
        Producto camisa = new Camisa();
        camisa.setNombre("Camisa Formal");
        camisa.setPrecioInicial(25.0);
        camisa.setEtiqueta(etiqueta);
        controladorProducto.leerProductos().add(camisa);
        controladorProducto.exportarProductosAJSON();

        //vaciamos la lista para simular que el programa acaba de iniciar
        controladorProducto.leerProductos().clear();
        controladorProducto.importarProductosDesdeJSON();

        //comprobar que el producto se ha importado correctamente
        List<Producto> productosImportados = controladorProducto.leerProductos();
        assertEquals(1, productosImportados.size(), "Debe haber un producto importado");
        assertEquals("Camisa Formal", productosImportados.get(0).getNombre());
        assertEquals(25.0, productosImportados.get(0).getPrecioInicial());
    }

    //test que prueba importar de un archivo que no existe
    @Test
    void importarProductosIncorrectoArchivoNoExiste() {
        //aseguramos que el archivo no existe
        File file = new File("productos.json");
        if (file.exists()) file.delete();

        //creamos la exception que lanzará el metodo importar
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controladorProducto.importarProductosDesdeJSON();
        });

        assertTrue(exception.getMessage().contains("Error al importar"),
                "Debe lanzar error si el fichero no existe");
    }

    //TEST EXPORTAR E IMPORTAR ETIQUETAS EN CSV
    @Test
    void exportarEtiquetasCSVCorrecto() {
        //crear la etiqueta y exportar
        controladorProducto.getListaEtiquetas().add(etiqueta);
        controladorProducto.exportarEtiquetasCSV();

        //comprobar que existe el archivo y que tiene contenido
        File archivo = new File("etiquetas.csv");
        assertTrue(archivo.exists(), "El archivo CSV debe existir tras exportar");
        assertTrue(archivo.length() > 0, "El archivo CSV no debe estar vacío");
    }

    //NO HACER TEST DE EXPORTAR INCORRECTO YA QUE LA RUTA SIEMPRE VA A SER CORRECTA AL PONERLA EN EL METODO DIRECTAMENTE

    @Test
    void importarEtiquetasCSVCorrecto() {
        //crear etiqueta, exportarlo, limpiar la lista (para poder importar sin que esté ya dentro) e importar
        controladorProducto.getListaEtiquetas().add(new Etiqueta("Rebajas"));
        controladorProducto.exportarEtiquetasCSV();
        controladorProducto.getListaEtiquetas().clear();
        controladorProducto.importarEtiquetasCSV();

        //comprobamos que se ha importado
        List<Etiqueta> importadas = controladorProducto.getListaEtiquetas();
        assertEquals(1, importadas.size(), "Debe importar una etiqueta");

        //y comprobamos que sea la correcta la que se ha importado
        Etiqueta importada = importadas.get(0);
        assertEquals("Rebajas", importada.getNombre(), "El nombre de la etiqueta importada debe coincidir");
    }

    @Test
    void importarEtiquetasCSV_archivoNoExiste_lanzaExcepcion() {
        //asegurar que el archivo no existe
        File archivo = new File("etiquetas.csv");
        if (archivo.exists()) {
            archivo.delete();
        }

        //comprobamos que da exception al importar
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controladorProducto.importarEtiquetasCSV();
        });

        assertTrue(exception.getMessage().contains("Error al importar etiquetas"), "Debe lanzar una excepción con mensaje adecuado");
    }
}
