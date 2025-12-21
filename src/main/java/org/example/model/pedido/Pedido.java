package org.example.model.pedido;

import jakarta.persistence.*;
import org.example.model.Usuario;

import java.time.LocalDate;
import java.util.*;

// marca la clase como una entidad JPA (se persistirá en la base de datos)
@Entity
// nombre de la tabla en la base de datos
@Table(name = "pedido")
public class Pedido {
    // identificador único de la entidad
    @Id
    // el valor del ID se genera automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //no hace falta transient por la referencia circular
    private long id;

    // campo obligatorio
    @Column(nullable = false)
    private LocalDate fecha;

    // persistencia de un enum como texto
    @Enumerated(EnumType.STRING)
    // campo obligatorio
    @Column(nullable = false)
    private EstadoPedido estado;

    // muchos pedidos pueden pertenecer a un mismo usuario
    // lazy: el producto no se carga hasta que se accede explícitamente
    @ManyToOne(fetch = FetchType.LAZY)
    // columna FK explícita en la tabla pedido
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // uso una lista para poder tener muchas lineasPedido en un producto (pueden repetirse)
    @OneToMany(
            // mappedby indica que la FK está en LineaPedido (campo pedido)
            mappedBy = "pedido",
            // lazy: el producto no se carga hasta que se accede explícitamente
            fetch = FetchType.LAZY,
            // persist, merge y remove se propagan a las líneas
            cascade = CascadeType.ALL,
            // orphanRemoval, si se elimina una línea de la colección se borra de BD
            orphanRemoval = true
    )
    private Set<LineaPedido> lineasPedido = new HashSet<>();


    // constructor
    // no id
    public Pedido(Usuario usuario) {
        this.usuario = usuario;
        //poner fecha del día que se hace el pedido, con el Date se guarda automáticamente
        this.fecha = LocalDate.now();
        //el pedido está pendiente ya que se ha creado
        this.estado = EstadoPedido.PENDIENTE;
    }

    // constructor vacío obligatorio para JPA
    public Pedido() {
    }


    // getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<LineaPedido> getLineasPedido() {
        return lineasPedido;
    }

    public void setLineasPedido(Set<LineaPedido> lineasPedido) {
        this.lineasPedido = lineasPedido;
    }


    // este metodo nos devuelve la suma de las líneas del pedido
    // cogemos la lista de líneas de pedido, obtenemos los subtotales de cada uno y los sumamos
    public double getPrecioTotal() {
        return lineasPedido.stream()
                //double ya que hay que devolver double
                .mapToDouble(LineaPedido::getPrecioSubTotal)
                //sumar
                .sum();
    }


    // métodos de cambiar estado (corrección profesor) NO SE CAMBIA EN EL CONTROLADOR, EL CONTROLADOR SOLO DIRIGE
    public void finalizar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede finalizar un pedido pendiente.");
        }
        estado = EstadoPedido.FINALIZADO;
    }

    public void cancelar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede cancelar un pedido pendiente.");
        }
        estado = EstadoPedido.CANCELADO;
    }

    public void entregar() {
        if (estado != EstadoPedido.FINALIZADO) {
            throw new IllegalStateException("Solo se puede entregar pedidos finalizados.");
        }
        estado = EstadoPedido.ENTREGADO;
    }


    // poner hasCode y equals porque en otras clases tengo Set de pedido
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return id == pedido.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
