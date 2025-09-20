package org.example.model.producto;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Etiqueta {
    private long id;
    private String nombre;
    private LocalDate fechaCreacion;
    //un conjunto para que no se repitan, una etiqueta puede ser de muchos productos (pero que no se repitan)
    private Set<Producto> productos = new HashSet<>();

    //constructor
    public Etiqueta(long id, String nombre, LocalDate fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
    }


}
