package com.example.wordy;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Sesión del usuario (equivale a sessionStorage 'wordy_sesion' de la web: usuario, id_usuario, id_rol).
 * El AVANCE no se guarda aquí: vive en WordyDB y se pide a la API cada vez.
 * Solo se guardan en caché la racha y las estrellas para pintar rápido el menú lateral.
 */
public final class SesionManager {

    /** Valores de la tabla ROL. */
    public static final int ROL_TUTOR = 1;
    public static final int ROL_ADMIN = 2;
    public static final int ROL_NINO = 3;
    public static final int ROL_PSICOPEDAGOGO = 4;

    private SesionManager() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext()
                .getSharedPreferences("wordy_sesion", Context.MODE_PRIVATE);
    }

    public static void iniciar(Context c, String idUsuario, String nombre, int idRol) {
        prefs(c).edit().clear()
                .putString("id", idUsuario)
                .putString("nombre", nombre)
                .putInt("rol", idRol)
                .apply();
    }

    /** USUARIO.id_usuario (es el mismo número que PERFIL_NINO.id_nino). */
    public static String usuarioId(Context c) {
        return prefs(c).getString("id", null);
    }

    public static String nombre(Context c) {
        return prefs(c).getString("nombre", "");
    }

    public static int rol(Context c) {
        return prefs(c).getInt("rol", -1);
    }

    public static boolean esTutor(Context c) {
        return rol(c) == ROL_TUTOR;
    }

    // ----- caché de la pantalla de inicio -----
    public static void guardarResumen(Context c, int racha, int estrellas) {
        prefs(c).edit().putInt("racha", racha).putInt("estrellas", estrellas).apply();
    }

    public static int racha(Context c) {
        return prefs(c).getInt("racha", 0);
    }

    public static int estrellas(Context c) {
        return prefs(c).getInt("estrellas", 0);
    }

    public static void cerrar(Context c) {
        prefs(c).edit().clear().apply();
    }
}
