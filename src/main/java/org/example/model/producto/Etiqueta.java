package org.example.model.producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Etiqueta {
    //no id en JSON
    @JsonIgnore
    //excluimos id para CSV (no lo anotamos)
    private long id;
    //para CSV
    @CsvBindByName(column = "nombre")
    private String nombre;
    //para CSV
    @CsvBindByName(column = "fechaCreacion")
    @CsvDate("dd/MM/yyyy") // formato español
    private LocalDate fechaCreacion;
    //evitar errores JSON y CSV
    @JsonIgnore
    //un conjunto para que no se repitan, una etiqueta puede ser de muchos productos (pero que no se repitan)
    private transient Set<Producto> productos = new HashSet<>();

    //constructor
    //vacío para JSON y CSV
    public Etiqueta() {
    }

    //no id
    //no poner productos ya que solo estamos creando la etiqueta, no asignando productos a etiquetas
    public Etiqueta(String nombre) {
        this.nombre = nombre;
        //fecha del día en que se crea
        this.fechaCreacion = LocalDate.now();
    }

    //getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Set<Producto> getProductos() {
        return productos;
    }

    public void setProductos(Set<Producto> productos) {
        this.productos = productos;
    }

    //hasCode y equals por el Set
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Etiqueta etiqueta = (Etiqueta) o;
        return id == etiqueta.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
