package com.example.wordy;

import android.content.Context;
import android.content.SharedPreferences;

public final class SesionManager {

    private SesionManager() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext()
                .getSharedPreferences("wordy_sesion", Context.MODE_PRIVATE);
    }

    public static void iniciar(Context c, String id, String nombre, int tipoCuenta) {
        prefs(c).edit().putString("id", id).putString("nombre", nombre)
                .putInt("tipo", tipoCuenta).apply();
    }

    public static String usuarioId(Context c) {
        return prefs(c).getString("id", null);
    }

    public static void cerrar(Context c) {
        prefs(c).edit().clear().apply();
    }
}