package com.example.wordy;

import java.util.Set;

/**
 * Contrato de almacenamiento del progreso de juego.
 * Equivale a las tablas PROGRESO_CUENTO y PALABRA_DESBLOQUEADA de la BD.
 * Para migrar a la API solo se crea otra clase que lo implemente.
 */
public interface JuegoRepository {

    // PROGRESO_CUENTO (estrellas_ganadas: 0 = no completado, 1..3)
    int cuentoEstrellas(int nivel);
    void guardarCuento(int nivel, int estrellas, int aciertosHuecos, int erroresHuecos,
                       int aciertosPreguntas, int erroresPreguntas);

    // PALABRA_DESBLOQUEADA
    boolean palabraDesbloqueada(String idPalabra);
    void desbloquearPalabra(String idPalabra);
    Set<String> palabrasDesbloqueadas();
    int vecesRepasada(String idPalabra);
    void registrarRepaso(String idPalabra);
}