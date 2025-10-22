package org.example.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.producto.Producto;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GestorFicherosJSON {

    //añadir las dependencias antes
    private static final ObjectMapper mapper = new ObjectMapper();

    //si no da error
    static {
        mapper.registerModule(new JavaTimeModule());
    }

    //METODO EXPORTAR DE PRODUCTOS A JSON
    public static void exportarProductosAJSON(List<Producto> listaProductos, String rutaFichero) {
        try {
            mapper.writerFor(new TypeReference<List<Producto>>() {}).writeValue(new File(rutaFichero), listaProductos);
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar productos a JSON: " + e.getMessage(), e);
        }
    }


    //METODO IMPORTAR DE JSON A OBJETOS
    public static List<Producto> importarProductosDesdeJSON(String rutaFichero) {
        try {
            return mapper.readValue(new File(rutaFichero),
                    mapper.getTypeFactory().constructCollectionType(List.class, Producto.class));
        } catch (IOException e) {
            throw new RuntimeException("Error al importar productos desde JSON: " + e.getMessage(), e);
        }
    }

}
