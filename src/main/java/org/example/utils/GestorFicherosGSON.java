package org.example.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.pedido.Pedido;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

//esta ya no admite herencia
public class GestorFicherosGSON {
    //metodo para exportar pedidos a JSOn a través de GSON
    public static void exportarPedidosAGson(List<Pedido> listaPedidos, String rutaArchivo) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            gson.toJson(listaPedidos, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar pedidos a JSON con Gson: " + e.getMessage(), e);
        }
    }

    //metodo para importar de GSON a pedidos
    //array por ser lista
    public static List<Pedido> importarPedidosDesdeGson(String rutaArchivo) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        try (FileReader reader = new FileReader(rutaArchivo)) {
            Pedido[] arrayPedidos = gson.fromJson(reader, Pedido[].class);
            return List.of(arrayPedidos);
        } catch (IOException e) {
            throw new RuntimeException("Error al importar pedidos desde JSON con Gson: " + e.getMessage(), e);
        }
    }
}
