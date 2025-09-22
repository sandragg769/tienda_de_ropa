package org.example.controller;

import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.util.ArrayList;
import java.util.List;

public class ControladorUsuario {
    private List<Usuario> listaUsuariosRegistrados = new ArrayList<>();
    //para asignar id a usuarios al registrar
    private long contadorUsuarios = 0;
    //usuario logueado para metodos de favoritos
    private Usuario usuarioLogueado;

    //registrar un usuario en la lista de usuarios registrados (y poner id)
    public Usuario registrarUsuario(Usuario usuario) {
        //miramos si existe ya un usuario con ese email
        for (Usuario usu : listaUsuariosRegistrados) {
            if (usu.getEmail().equalsIgnoreCase(usuario.getEmail())) {
                throw new IllegalArgumentException("Ya existe un usuario con ese email.");
            }
        }
        //si no existe (no salta la exception ya incrementamos el contador ya que vamos a añadir un usuario
        //le ponemos el id
        contadorUsuarios++;
        //le ponemos el id al usuario a registrar
        usuario.setId(contadorUsuarios);
        //metemos el usuario a la lista
        listaUsuariosRegistrados.add(usuario);
        return usuario;
    }

    //metodo para loguear un usuario
    public Usuario login(String email, String password) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados un email y password igual a los pasados por parámetro, si lo encuentra lo devuelve
            if (usu.getEmail().equalsIgnoreCase(email) && usu.getPasssword().equals(password)) {
                //ponemos este usuario en usuarioLogueado (para hacer cosas en la cuenta hay que estar logueado por lo que será necesario este metodo
                usuarioLogueado = usu;
                return usu;
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se encuentra email o password registrado.");
    }

    //metodo para leer usuarios
    public List<Usuario> leerUsuarios() {
        return listaUsuariosRegistrados;
    }

    //metodo para enontrar un usuario con un id específico
    public Usuario leerUsuarioPorId(long id) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados un email y password igual a los pasados por parámetro, si lo encuentra lo devuelve
            if (usu.getId() == id) {
                return usu;
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se encuentra el usuario con esa Id.");
    }

    //metodo para eliminar usuarios de la lista de usuarios registrados
    public void eliminarUsuario(Usuario usuario) {
        Usuario aEliminar;
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados el id ya que al estar registrado tiene id y si lo encuentra lo elimina
            if (usu.getId() == usuario.getId()) {
                listaUsuariosRegistrados.remove(usu);
                //si esto pasa que se salga de la lista, si no se pone siempre da exception de abajo
                return;
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se puede eliminar el usuario ya que no se encuentra ese Id.");
    }

    //metodo para actualizar perfil de usuario, se busca id ya que ese no cambia
    //HAY QUE BLOQUEAR EL QUE SE PUEDA CAMBIAR EL DNI, NI EL ID, NI LA FECHANACIMIENTO PORQUE SI NO EN LOS TEST DA FALLO
    public Usuario actualizarUsuario(Usuario usuarioNuevo) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados el id ya que al estar registrado tiene id y si lo encuentra lo elimina
            if (usu.getId() == usuarioNuevo.getId()) {
                //NO CAMBIAR DNI
                if (!usu.getDni().equalsIgnoreCase(usuarioNuevo.getDni())) {
                    throw new IllegalArgumentException("No se puede cambiar el DNI de un usuario registrado.");
                }
                //NO CAMBIAR FECHANACIMIENTO
                if (!usu.getFechaNacimiento().equals(usuarioNuevo.getFechaNacimiento())) {
                    throw new IllegalArgumentException("No se puede cambiar la fecha de nacimiento de un usuario registrado.");
                }
                //NO CAMBIAR ID
                if (usuarioNuevo.getId() != usu.getId()) {
                    throw new IllegalArgumentException("No se puede cambiar el ID de un usuario.");
                }
                usu.setDireccion(usuarioNuevo.getDireccion());
                usu.setEmail(usuarioNuevo.getEmail());
                usu.setPasssword(usuarioNuevo.getPasssword());
                usu.setTelefono(usuarioNuevo.getTelefono());
                //devuelve el usuario cuando lo cambia
                return usu;
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se puede actualizar el usuario ya que no se encuentra ese Id.");
    }

    //metodo para añadir un producto a la lista de favoritos, en el controladorUsuario ya que es el que tiene la lista de favoritos
    //se pasa directamente el Usuario asique no hay que leerUsuarioPorId
    public void añadirProductoFavorito(Producto producto, Usuario usuario) {
        //ya que es un Set favoritos simplemente añadimos el producto no hace falta ver si está o no
        usuario.getFavoritos().add(producto);
        //no lanzar exception si ya está el producto dentro, simplemente no se añade y ya
        //IMPORTANTE, añadir también el usuario en el Set de UsuariosProductosFavoritos para que el producto sepa de quien es favorito (bidireccional)
    }

    //metodo para eliminar un producto de favoritos
    public void eliminarProductoFavorito(Producto producto, Usuario usuario) {
        //ya que es un Set favoritos simplemente añadimos el producto no hace falta ver si está o no
        usuario.getFavoritos().remove(producto);
        //no lanzar exception si ya está el producto dentro, simplemente no se añade y ya
        //IMPORTANTE, quitar el usuario del Set usuariosProductosFavoritos del producto ya que es bidireccional y tienen q saber los dos que se ha quitado, ya que si no es producto no sabria de q usuario es favorito
        producto.getUsuariosProductosFavoritos().remove(usuario);
    }

}
