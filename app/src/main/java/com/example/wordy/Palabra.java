package com.example.wordy;

import java.util.Locale;
import java.util.regex.Pattern;

/** Modelo de una palabra (equivale a la tabla PALABRA). */
public class Palabra {

    public final String id;
    public final int nivel;
    public final String palabra;
    public final String corto;        // significado corto para el ejercicio de sílabas
    public final String significado;
    public final String ejemplo;
    public final String[] silabas;
    public final int falta;           // índice de la sílaba que falta
    public final String[] distractores;
    public final int imagen;          // drawable (0 = sin imagen)

    public Palabra(String id, int nivel, String palabra, String corto, String significado,
                   String ejemplo, String[] silabas, int falta, String[] distractores, int imagen) {
        this.id = id;
        this.nivel = nivel;
        this.palabra = palabra;
        this.corto = corto;
        this.significado = significado;
        this.ejemplo = ejemplo;
        this.silabas = silabas;
        this.falta = falta;
        this.distractores = distractores;
        this.imagen = imagen;
    }

    public String silabaFaltante() {
        return silabas[falta];
    }

    /** Ej.: "BA-____" */
    public String palabraIncompleta() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < silabas.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(i == falta ? "____" : silabas[i].toUpperCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /** El ejemplo con la palabra tapada (se usa como pista). */
    public String ejemploOculto() {
        return Pattern.compile(Pattern.quote(palabra), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(ejemplo).replaceAll("_____");
    }
}