package org.example.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

    //METODO EXPORTAR DE OBJETOS A JSON
    //para que en el futuro se pueda usar apra otras cosas no se pone como tal aquí que es para productos
    //se pone luego en el controlador, por eso no se pone un tipado específico a la lista y demás
    public static <T> void exportarAJSON(List<T> lista, String rutaFichero) {
        try {
            //typereference por lista
            mapper.writerFor(new TypeReference<List<T>>() {
            }).writeValue(new File(rutaFichero), lista);
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar a JSON: " + e.getMessage(), e);
        }
    }

    //METODO IMPORTAR DE JSON A OBJETOS
    public static <T> List<T> importarDesdeJSON(String rutaFichero, Class<T> tipo) {
        try {
            return mapper.readValue(new File(rutaFichero),
                    mapper.getTypeFactory().constructCollectionType(List.class, tipo));
        } catch (IOException e) {
            throw new RuntimeException("Error al importar desde JSON: " + e.getMessage(), e);
        }
    }

}
