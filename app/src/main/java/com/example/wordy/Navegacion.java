package com.example.wordy;

import android.app.Activity;
import android.content.Intent;

public final class Navegacion {

    private Navegacion() {}

    public static void irAHome(Activity a) {
        Intent i = new Intent(a, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
        a.finish();
    }

    /** Botón "¡Continuar aventura!": abre la siguiente actividad pendiente. */
    public static void continuarAventura(Activity a) {
        JuegoManager juego = new JuegoManager(new JuegoLocal(a, SesionManager.usuarioId(a)));
        Intent i;
        int cuento = juego.siguienteNivelCuentos();
        if (cuento > 0) {
            i = new Intent(a, CuentoLecturaActivity.class);
            i.putExtra(CuentoLecturaActivity.EXTRA_NIVEL, cuento);
        } else {
            int palabras = juego.siguienteNivelPalabras();
            if (palabras > 0) {
                i = new Intent(a, PracticaPalabrasActivity.class);
                i.putExtra(PracticaPalabrasActivity.EXTRA_NIVEL, palabras);
            } else {
                i = new Intent(a, AprendePalabrasActivity.class);
            }
        }
        a.startActivity(i);
    }
}