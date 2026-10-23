package com.example.wordy;

/** Contrato de almacenamiento. Para la BD solo se crea otra clase que lo implemente. */
public interface ProgresoRepository {
    Usuario obtener(String id);
    void guardar(Usuario usuario);
}