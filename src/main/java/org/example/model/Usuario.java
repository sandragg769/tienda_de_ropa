package org.example.model;

import java.time.LocalDate;

public class Usuario {
    private long id;
    private String dni;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String passsword;

    //constructor
    public Usuario(long id, String dni, String direccion, LocalDate fechaNacimiento,
                   String telefono, String email, String passsword) {
        this.id = id;
        this.dni = dni;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        this.passsword = passsword;
    }

    //getters
    public long getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getPasssword() {
        return passsword;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getDni() {
        return dni;
    }

    //setters
    public void setId(long id) {
        this.id = id;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasssword(String passsword) {
        this.passsword = passsword;
    }
}
