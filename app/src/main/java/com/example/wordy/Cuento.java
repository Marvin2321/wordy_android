package com.example.wordy;

import java.util.List;

/** Modelo de un cuento (equivale a CUENTO, CUENTO_ESCENA, HUECO_CUENTO y PREGUNTA_CUENTO). */
public class Cuento {

    public static class Hueco {
        public final String correcta;
        public final String[] opciones; // incluye la correcta

        public Hueco(String correcta, String[] opciones) {
            this.correcta = correcta;
            this.opciones = opciones;
        }
    }

    public static class Escena {
        public final String antes;   // texto que se narra antes del hueco (o la escena completa)
        public final Hueco hueco;    // null si la escena no tiene hueco
        public final String despues; // texto posterior al hueco

        public Escena(String antes, Hueco hueco, String despues) {
            this.antes = antes;
            this.hueco = hueco;
            this.despues = despues;
        }
    }

    public static class Pregunta {
        public final String enunciado;
        public final String[] opciones;
        public final int correcta; // índice en opciones

        public Pregunta(String enunciado, String[] opciones, int correcta) {
            this.enunciado = enunciado;
            this.opciones = opciones;
            this.correcta = correcta;
        }
    }

    public final int nivel;
    public final String titulo;
    public final List<Escena> escenas;
    public final List<Pregunta> preguntas;
    public final int imagenLectura;    // drawable al final del texto (0 = ninguna)
    public final int imagenPreguntas;  // drawable entre las preguntas (0 = ninguna)

    public Cuento(int nivel, String titulo, List<Escena> escenas, List<Pregunta> preguntas,
                  int imagenLectura, int imagenPreguntas) {
        this.nivel = nivel;
        this.titulo = titulo;
        this.escenas = escenas;
        this.preguntas = preguntas;
        this.imagenLectura = imagenLectura;
        this.imagenPreguntas = imagenPreguntas;
    }

    public int totalHuecos() {
        int n = 0;
        for (Escena e : escenas) if (e.hueco != null) n++;
        return n;
    }
}