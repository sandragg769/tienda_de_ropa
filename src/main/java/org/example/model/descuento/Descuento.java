package org.example.model.descuento;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import org.example.model.producto.Producto;

// añadir anotación a descuento (corrección profe)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DescuentoFijo.class, name = "DescuentoFijo"),
        @JsonSubTypes.Type(value = DescuentoPorcentaje.class, name = "DescuentoPorcentaje")
})

// marca la clase como una entidad JPA (se persistirá en la base de datos)
@Entity
// define jerarquía de herencia JPA usando una única tabla para todas las subclases
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// columna que JPA usará para saber que subclase concreta es cada fila
@DiscriminatorColumn(name = "tipo_descuento")
public abstract class Descuento {
    // identificador único de la entidad
    @Id
    // el valor del ID se genera automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relación con producto (1 producto → 0..1 descuento)
    // mappedBy indica que esta entidad no es la dueña de la relación
    // la clave foránea está en la entidad Producto
    @OneToOne(mappedBy = "descuento")
    protected Producto producto;


    // constructor vacío obligatorio para JPA
    protected Descuento() {
    }


    // getters y setters
    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }


    // metodo común que implementan las subclases
    public abstract double calcularMontoDescuento(Producto producto);
}
