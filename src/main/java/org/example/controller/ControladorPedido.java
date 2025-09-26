package org.example.controller;

import org.example.model.Usuario;
import org.example.model.pedido.EstadoPedido;
import org.example.model.pedido.LineaPedido;
import org.example.model.pedido.Pedido;
import org.example.model.producto.Producto;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ControladorPedido {
    private List<Pedido> listaPedidos = new ArrayList<>();
    private long contadorPedidos = 0;
    //el contador de lineaPedido también se controla aquí
    private long contadorLineaPedido = 0;

    //METODOS CRUD PEDIDO
    //metodo para crear un pedido, para esto hay que saber que usuario lo crea
    public Pedido crearPedido(Usuario usuario) {
        //creamos el pedido
        Pedido pedido = new Pedido(usuario);
        //le asignamos un id
        pedido.setId(contadorPedidos++);
        //cambiar estado a PENDIENTE
        pedido.setEstado(EstadoPedido.PENDIENTE);
        //añadimos a la lista de pedidos
        listaPedidos.add(pedido);
        //IMPORTANTE añadir al pedido a la lista de pedidos de ese usuario en concreto
        usuario.getPedidos().add(pedido);
        //devolvemos el pedido
        return pedido;
    }

    //metodo para borrar un pedido
    public void borrarPedido(long id) {
        //buscar pedido por id
        for (Pedido pedido : listaPedidos) {
            if (pedido.getId() == id) {
                //si lo encuentra lo elimina y hacer un return para que se salga del for
                listaPedidos.remove(pedido);
                //IMPORTANTE también lo eliminamos del usuario
                pedido.getUsuario().getPedidos().remove(pedido);
                return;
            }
        }
    }

    //metodo para leer todos los pedidos
    public List<Pedido> leerPedidos() {
        return listaPedidos;
    }

    //metodo para encontrar un pedido por id, devuelve el pedido
    public Pedido leerPedidoPorId(long id) {
        for (Pedido pedido : listaPedidos) {
            //busca en la lista el pedido con ese id y lo devuelve
            if (pedido.getId() == id) {
                return pedido;
            }
        }
        //si no devuelve el pedido antes lanza exception
        throw new IllegalArgumentException("No se encuentra el pedido con esa Id.");
    }

    //metodo para actualizar datos del pedido (no el estado, eso lo hacen métodos especificos, le pasamos el pedido nuevo y que lo cambie con setters
    public Pedido actualizarDatosPedido(Pedido pedidoNuevo) {
        //localizamos el pedidoDesactualizado buscandolo por id (pilla la del nuevo)
        Pedido pedidoDesactualizado = leerPedidoPorId(pedidoNuevo.getId());
        //controlamos que no se cambie el id ni el estado
        //NO CAMBIAR ID
        if (pedidoNuevo.getId() != pedidoDesactualizado.getId()) {
            throw new IllegalArgumentException("No se puede cambiar el ID de un pedido.");
        }
        //NO CAMBIAR ESTADO
        if (pedidoNuevo.getEstado() != pedidoDesactualizado.getEstado()) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un pedido.");
        }
        //a este pedido le cambiamos lo que se pueda cambiar con setters
        pedidoDesactualizado.setFecha(pedidoNuevo.getFecha());
        pedidoDesactualizado.setUsuario(pedidoNuevo.getUsuario());
        //devolvemos el pedido viejo el cual ya está actualizado
        return pedidoDesactualizado;
    }

    //METODOS QUE TIENEN QUE VER CON EL ESTADO DEL PEDIDO
    //se le pasa el Usuario ya que es el que actua sobre esto, por ejemplo entregar no, porque ahí el cliente ya no tiene nada que ver
    public Pedido finalizarPedido(Usuario usuario) {
        //finalizamos el pedido
        Pedido pedido = encontrarPedidoPendienteDeUSuarioConcreto(usuario);
        pedido.setEstado(EstadoPedido.FINALIZADO);
        //devuelve el pedido
        return pedido;
    }

    //metodo para cancelar un pedido (antes de ser finalizado o entregado)
    public Pedido cancelarPedido(Usuario usuario) {
        //cancelamos el pedido si encuentra un pedido pendiente del usuario
        Pedido pedido = encontrarPedidoPendienteDeUSuarioConcreto(usuario);
        pedido.setEstado(EstadoPedido.CANCELADO);
        //devuelve el pedido
        return pedido;
    }

    //metodo para entregar pedido (solo si está finalizado)
    //ya no necesitamos el Usuario pq esto es totalmente aparte de este, lo localizamos por id como hemos hecho en los demás metodos
    public Pedido entregarPedido(long id) {
        Pedido pedidoEntregar = leerPedidoPorId(id);
        //si está finalizado cambiamos a entregado
        if (pedidoEntregar.getEstado() == EstadoPedido.FINALIZADO) {
            pedidoEntregar.setEstado(EstadoPedido.ENTREGADO);
            return pedidoEntregar;
        } else {
            throw new IllegalArgumentException("Solo se puede entregar pedidos finalizados.");
        }
    }


    //METODOS DE LINEAS DE PEDIDO
    //aquí no se puede usar el metodo auxiluiar pq si no tiene pedido pendiuente lanza exception y no deja que siga el metodo y se cree el pedido pendiente!!!
    public LineaPedido añadirLineaPedidoAPedido(Usuario usuario, Producto producto, int cantidad) {
        //buscamos el pedido pendiente del usuario sin dar exception
        Optional<Pedido> pedidoOptional = listaPedidos.stream().filter(it -> it.getUsuario().equals(usuario) && it.getEstado() == EstadoPedido.PENDIENTE).findFirst();

        //si no encuentra pedido lo crea
        //con el orElseGet devuelve el pedido si está presente
        Pedido pedido = pedidoOptional.orElseGet(() -> {
            //se crea el pedido nuevo (lo guardamos para ponerlo
            crearYAñadirLineaPedido(nuevoPedido,producto,cantidad);

        });

        // Se haya creado ahora o ya existía el pedido pendiente, se crea y añade la línea de pedido
        return crearYAñadirLineaPedido(pedido, producto, cantidad);
    }

    //metodo para leer lineas de pedido de UN pedido concreto
    public Set<LineaPedido> leerLineasPedidoDeDedidoConcreto(Usuario usuario) {
        Pedido pedido = encontrarPedidoPendienteDeUSuarioConcreto(usuario);
        return pedido.getLineasPedido();
    }

    //metodo para actualizar lineas de pedido de UN pedido concreto
    public Set<LineaPedido> actualizarLineasPedidoDePedidoConcreto(Usuario usuario, LineaPedido lineaPedidoNueva) {
        //buscar pedido pendiente
        Pedido pedido = encontrarPedidoPendienteDeUSuarioConcreto(usuario);
        //actualizamos este pedido por el nuevo
        //bucamos la línea concretamente
        for (LineaPedido lineaPedido : pedido.getLineasPedido()) {
            if (lineaPedido.getId() == lineaPedidoNueva.getId()) {
                //NO CAMBIAR ID
                if (lineaPedidoNueva.getId() != lineaPedido.getId()) {
                    throw new IllegalArgumentException("No se puede cambiar el ID de una línea de pedido..");
                }
                //NO CAMBIAR PRODUCTO (no tiene sentido ya que si no quitas el producto y pones el nuevo)
                if (lineaPedidoNueva.getProducto() != lineaPedido.getProducto()) {
                    throw new IllegalArgumentException("No se puede cambiar el producto de una línea de pedido.");
                }
                //a esta línea de pedido le cambiamos lo que se pueda cambiar con setters
                lineaPedido.setCantidad(lineaPedidoNueva.getCantidad());
            }
        }
        return pedido.getLineasPedido();
    }

    //metodo para eliminar una línea de pedido de UN pedido concreto
    public void eliminarLineaPedidoDePedido(Usuario usuario, long idLinea) {
        //buscamos el pedido de la línea
        Pedido pedido = encontrarPedidoPendienteDeUSuarioConcreto(usuario);
        //buscamos la línea a eliminar
        for (LineaPedido lineaPedido : pedido.getLineasPedido()) {
            if (lineaPedido.getId() == idLinea) {
                //quitamos la línea
                pedido.getLineasPedido().remove(lineaPedido);
                //IMPORTANTE obtenemos el producto de la línea y ponemos la línea null (bidireccional)
                lineaPedido.getProducto().setLineaPedido(null);
                //cuando lo haga que se salga del metodo
                return;
            }
        }

        //si no encuentra ninguno da exception
        throw new IllegalArgumentException("El usuario no tiene pedido pendiente.");
    }

    //METODO AUXILIAR
    //metodo para no repetir el crear y añadir una línea de pedido en el metodo de añadir línea pedido
    public LineaPedido crearYAñadirLineaPedido(Pedido pedido, Producto producto, int cantidad) {
        LineaPedido lineaPedido = new LineaPedido(cantidad, producto);
        lineaPedido.setId(contadorLineaPedido++);
        pedido.getLineasPedido().add(lineaPedido);
        //IMPORTANTE la línea de pedido también se añade al producto (bidireccional)
        producto.setLineaPedido(lineaPedido);
        return lineaPedido;
    }

    //metodo para buscar el pedido pendiente de un usuario
    public Pedido encontrarPedidoPendienteDeUSuarioConcreto(Usuario usuario) {
        for (Pedido pedido : listaPedidos) {
            if (pedido.getUsuario().equals(usuario) && pedido.getEstado() == EstadoPedido.PENDIENTE) {
                return pedido;
            }
        }
        //si no encuentra ninguno da exception
        throw new IllegalArgumentException("No se encuentra el pedido pendiente del usuario.");
    }

}
