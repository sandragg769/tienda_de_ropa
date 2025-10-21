package org.example.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

//esta ya no admite herencia
public class GestorFicherosGSON {
    //metodo para exportar objetos a GSON
    public static <T> void exportarListaAGson(List<T> lista, String rutaArchivo) {
        //crear el builder
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        //serializacion y guardar
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar datos a JSON con Gson: " + e.getMessage(), e);
        }
    }

    //metodo para importar de GSON a objeto
    //array por ser lista
    public static <T> List<T> importarListaDesdeGson(String rutaArchivo, Class<T[]> claseArray) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        try (FileReader reader = new FileReader(rutaArchivo)) {
            T[] array = gson.fromJson(reader, claseArray);
            return List.of(array);
        } catch (IOException e) {
            throw new RuntimeException("Error al importar datos desde JSON con Gson: " + e.getMessage(), e);
        }
    }
}
