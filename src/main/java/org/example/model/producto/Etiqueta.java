package org.example.model.producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// marca la clase como una entidad JPA (se persistirá en la base de datos)
@Entity
// nombre de la tabla en la base de datos
@Table(name = "etiqueta")
public class Etiqueta {
    // identificador único de la entidad
    @Id
    // el valor del ID se genera automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    // excluimos id para CSV (no lo anotamos)
    private long id;

    // para CSV
    @CsvBindByName(column = "nombre")
    // campo obligatorio
    @Column(nullable = false)
    private String nombre;

    // para CSV
    @CsvBindByName(column = "fechaCreacion")
    @CsvDate("dd/MM/yyyy") // formato español
    // campo obligatorio
    @Column(nullable = false)
    private LocalDate fechaCreacion;

    // un conjunto para que no se repitan, una etiqueta puede ser de muchos productos (pero que no se repitan)
    // evitar errores JSON y CSV
    @JsonIgnore
    // mappedBy indica que el dueño es Producto (campo etiqueta)
    // eager: cuando cargues un usuario en JPA automáticamente carga también la etiqueta
    @OneToMany(mappedBy = "etiqueta", fetch = FetchType.EAGER)
    private Set<Producto> productos = new HashSet<>();

    // constructor vacío obligatorio para JPA
    public Etiqueta() {
    }

    // constructor
    // no id
    // no poner productos ya que solo estamos creando la etiqueta, no asignando productos a etiquetas
    public Etiqueta(String nombre) {
        this.nombre = nombre;
        //fecha del día en que se crea
        this.fechaCreacion = LocalDate.now();
    }


    // getters y setters
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

    // hasCode y equals por el Set
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
