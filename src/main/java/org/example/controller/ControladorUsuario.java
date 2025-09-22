package org.example.controller;

import org.example.model.Usuario;
import org.example.model.producto.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ControladorUsuario {
    private List<Usuario> listaUsuariosRegistrados = new ArrayList<>();
    //para asignar id a usuarios al registrar
    private long contadorUsuarios = 0;
    //usuario logueado para metodos de favoritos
    private Optional<Usuario> usuarioOptinal;

    //registrar un usuario en la lista de usuarios registrados (y poner id)
    public Usuario registrarUsuario(Usuario usuario) {
        //miramos si existe ya un usuario con ese email
        for (Usuario usu : listaUsuariosRegistrados) {
            if (usu.getEmail().equals(usuario.getEmail())) {
                throw new IllegalArgumentException("Ya está registrado este ususario.");
            }
        }
        //si no existe (no salta la exception ya incrementamos el contador ya que vamos a añadir un usuario
        contadorUsuarios++;
        //le ponemos el id
        usuario.setId(contadorUsuarios);
        //metemos el usuario a la lista
        listaUsuariosRegistrados.add(usuario);
        return usuario;
    }

    //metodo para loguear un usuario
    public Usuario login(String email, String password) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados un email y password igual a los pasados por parámetro, si lo encuentra lo devuelve
            if (usu.getEmail().equals(email) && usu.getPasssword().equals(password)) {
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

    public Optional<Usuario> leerUsuarioPorId(long id) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados un email y password igual a los pasados por parámetro, si lo encuentra lo devuelve
            if (usu.getId() == id) {
                //envuelve el usuario en un Optional
                return Optional.of(usu);
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se encuentra el usuario con esa Id.");
    }

    //metodo para eliminar usuarios de la lista de usuarios registrados
    public void eliminarUsuario(Usuario usuario) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados el id ya que al estar registrado tiene id y si lo encuentra lo elimina
            if (usu.getId() == usuario.getId()) {
                listaUsuariosRegistrados.remove(usu);
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se puede eliminar el usuario ya que no se encuentra ese Id.");
    }

    //metodo para actualizar perfil de usuario, se busca id ya que ese no cambia
    public Usuario actualizarUsuario(Usuario usuario) {
        for (Usuario usu : listaUsuariosRegistrados) {
            //busca en la lista de registrados el id ya que al estar registrado tiene id y si lo encuentra lo elimina
            if (usu.getId() == usuario.getId()) {
                //no se puede cambiar id, ni dni, ni fecha de nacimiento
                usu.setDireccion(usuario.getDireccion());
                usu.setEmail(usuario.getEmail());
                usu.setPasssword(usuario.getPasssword());
                usu.setTelefono(usuario.getTelefono());
            }
        }
        //si no devuelve el usu antes lanza exception
        throw new IllegalArgumentException("No se puede actualizar el usuario ya que no se encuentra ese Id.");
    }

    //metodo para añadir un producto a la lista de favoritos, en el controladorUsuario ya que es el que tiene la lista de favoritos
    public void añadirProductoFavorito(Producto producto, Usuario usuario) {
        //guardar el usuario en el usuarioRegistrado, para verificar que está registrado
        usuarioOptinal = leerUsuarioPorId(usuario.getId());

        //localizar lista de favoritos de nuestro usuario y ver que no tiene ya el producto en favoritos
        if (!usuario.getFavoritos().contains(producto)) {
            usuario.getFavoritos().add(producto);
        }
        //no lanzar exception si ya está el producto dentro, simplemente no se añade y ya
    }

    //metodo para eliminar un producto de favoritos


}
