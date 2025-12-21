package org.example.model.descuento;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import org.example.model.producto.Producto;

//añadir anotación a descuento (corrección profe)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DescuentoFijo.class, name = "DescuentoFijo"),
        @JsonSubTypes.Type(value = DescuentoPorcentaje.class, name = "DescuentoPorcentaje")
})

// se va a persistir en la base de datos, corresponde a una tabla
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_descuento")
public abstract class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con producto (1 producto → 0..1 descuento)
    @OneToOne(mappedBy = "descuento")
    protected Producto producto;

    // constructor vacío obligatorio para JPA
    protected Descuento() {
    }

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
