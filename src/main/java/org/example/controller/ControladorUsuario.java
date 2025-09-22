package org.example.controller;

import com.sun.security.auth.UnixNumericUserPrincipal;
import org.example.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class ControladorUsuario {
    private List<Usuario> listaUsuariosRegistrados= new ArrayList<>();
    private long contadorUsuarios=0;

    //registrar un usuario en la lista de usuarios registrados (y poner id)
    public Usuario registrarUsuario (Usuario usuario) {
        //miramos si existe ya un usuario con ese email
        for (Usuario usu : listaUsuariosRegistrados) {
            if (usu.getEmail().equals(usuario.getEmail())) {
                throw new IllegalArgumentException("Ya está registrado este ususario");
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
}
