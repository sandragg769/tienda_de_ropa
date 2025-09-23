package org.example.controller;

import org.example.model.pedido.Pedido;

import java.util.ArrayList;
import java.util.List;

public class ControladorPedido {
    private List<Pedido> listaPedidos=new ArrayList<>();
    private long contadorPedidos=0;
    //el contador de lineaPedido también se controla aquí
    private  long contadorLineaPedido=0;

    //tenemos que tener el controlador para acceder a los usuarios para usar sus métodos
    private ControladorUsuario controladorUsuario;

}
