package org.example.model.pedido;

import org.example.model.Usuario;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {
    private long id;
    private Date fecha;
    private EstadoPedido estado;
    //creo un objeto y no una lista porque un pedido es tenido por un usuario solo
    private Usuario usuario;
    //uso una lista para poder tener muchas lineasPedido en un producto (pueden repetirse)
    private List<LineaPedido> lineasPedido = new ArrayList<>();

    //constructor
    public Pedido(long id, Date fecha, EstadoPedido estado) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
    }


    //Este método nos devuelve la suma de las líneas del pedido
    public double getPecioTotal() {
    }
}
