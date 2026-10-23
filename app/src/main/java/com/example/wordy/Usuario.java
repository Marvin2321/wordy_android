package com.example.wordy;

/** Modelo: cada campo corresponde a una columna de la futura tabla. */
public class Usuario {
    public String id;          // PK
    public String nombre;
    public int tipoCuenta;     // posición del spinner
    public int racha = 0;
    public int estrellas = 0;
    public long ultimoDia = -1; // día de la última actividad (-1 = ninguna)

    public Usuario(String id, String nombre, int tipoCuenta) {
        this.id = id;
        this.nombre = nombre;
        this.tipoCuenta = tipoCuenta;
    }
}