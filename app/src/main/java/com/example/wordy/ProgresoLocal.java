package com.example.wordy;

import android.content.Context;
import android.content.SharedPreferences;

/** Implementación provisional con SharedPreferences. Reemplazar por SQLite/Room/API. */
public class ProgresoLocal implements ProgresoRepository {

    private final SharedPreferences prefs;

    public ProgresoLocal(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences("wordy_progreso", Context.MODE_PRIVATE);
    }

    @Override
    public Usuario obtener(String id) {
        Usuario u = new Usuario(id,
                prefs.getString(id + "_nombre", id),
                prefs.getInt(id + "_tipo", 0));
        u.racha = prefs.getInt(id + "_racha", 0);
        u.estrellas = prefs.getInt(id + "_estrellas", 0);
        u.ultimoDia = prefs.getLong(id + "_ultimoDia", -1);
        return u;
    }

    @Override
    public void guardar(Usuario u) {
        prefs.edit()
                .putString(u.id + "_nombre", u.nombre)
                .putInt(u.id + "_tipo", u.tipoCuenta)
                .putInt(u.id + "_racha", u.racha)
                .putInt(u.id + "_estrellas", u.estrellas)
                .putLong(u.id + "_ultimoDia", u.ultimoDia)
                .apply();
    }
}