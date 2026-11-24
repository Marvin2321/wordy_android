package com.example.wordy;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

/** Navegación común. El menú de hamburguesa es TEMPORAL, solo para saltar entre pantallas. */
public final class Navegacion {

    private Navegacion() {}

    public static void irAHome(Activity a) {
        Intent i = new Intent(a, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
        a.finish();
    }

    public static void mostrarMenu(Activity a) {
        String[] opciones = {"Inicio", "Cuenta cuentos", "Aprende palabras"};
        new AlertDialog.Builder(a)
                .setItems(opciones, (d, which) -> {
                    switch (which) {
                        case 0:
                            irAHome(a);
                            break;
                        case 1:
                            a.startActivity(new Intent(a, CuentaCuentosActivity.class));
                            break;
                        default:
                            a.startActivity(new Intent(a, AprendePalabrasActivity.class));
                            break;
                    }
                })
                .show();
    }
}