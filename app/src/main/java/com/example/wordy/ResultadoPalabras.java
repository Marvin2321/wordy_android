package com.example.wordy;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.util.List;

/** Resultado del nivel de Aprende palabras (mostrarResultado() de palabrasModulo.js), común a las 2 actividades. */
final class ResultadoPalabras {

    private ResultadoPalabras() {}

    static void mostrar(Activity a, JSONObject res, List<String> nuevas, Class<?> pantallaJuego) {
        int aciertos = res.optInt("aciertos");
        int total = res.optInt("total");
        int estrellas = res.optInt("estrellas");
        int nivel = res.optInt("nivel", 1);
        int siguiente = res.isNull("siguienteNivel") ? 0 : res.optInt("siguienteNivel", 0);

        StringBuilder e = new StringBuilder();
        for (int s = 1; s <= 3; s++) e.append(s <= estrellas ? "★" : "☆");

        StringBuilder msg = new StringBuilder(e);
        msg.append("\n\nAcertaste ").append(aciertos).append(" de ").append(total).append(" al primer intento.");
        if (!nuevas.isEmpty()) {
            msg.append("\n\nPalabras nuevas en tu diccionario:");
            for (String p : nuevas) msg.append("\n📖 ").append(p);
        }
        String m = ApiClient.txt(res, "mensaje");
        if (!m.isEmpty()) msg.append("\n\n").append(m);

        String titulo = aciertos == total ? "¡Lo hiciste perfecto!"
                : (aciertos * 2 >= total ? "¡Muy bien!" : "¡Buen intento, sigue practicando!");

        String actividad = pantallaJuego == RelacionarImagenesActivity.class
                ? AprendeNivelesActivity.IMAGENES : AprendeNivelesActivity.PRACTICA;

        AlertDialog.Builder b = new AlertDialog.Builder(a)
                .setTitle(titulo)
                .setMessage(msg.toString())
                .setCancelable(false)
                .setNeutralButton("Ver el mapa", (d, w) -> {
                    Intent i = new Intent(a, AprendeNivelesActivity.class);
                    i.putExtra(AprendeNivelesActivity.EXTRA_ACTIVIDAD, actividad);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    a.startActivity(i);
                    a.finish();
                })
                .setNegativeButton("Jugar otra vez", (d, w) -> abrir(a, pantallaJuego, nivel));
        if (siguiente > 0) b.setPositiveButton("Siguiente nivel", (d, w) -> abrir(a, pantallaJuego, siguiente));
        b.show();
    }

    private static void abrir(Activity a, Class<?> pantalla, int nivel) {
        Intent i = new Intent(a, pantalla);
        i.putExtra(PracticaPalabrasActivity.EXTRA_NIVEL, nivel);
        a.startActivity(i);
        a.finish();
    }
}
