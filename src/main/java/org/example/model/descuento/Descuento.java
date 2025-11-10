package org.example.model.descuento;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
public interface Descuento {
    //metodo a implementar en las otras clases de descuento
    //relación con producto (parámetro) no deja poner varios descuentos a un producto??
    double calcularMontoDescuento(Producto producto);
}
