package com.example.wordy;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Implementación provisional con SharedPreferences (una por usuario). */
public class JuegoLocal implements JuegoRepository {

    private final SharedPreferences prefs;
    private final String usuario;

    public JuegoLocal(Context context, String usuarioId) {
        prefs = context.getApplicationContext()
                .getSharedPreferences("wordy_juego", Context.MODE_PRIVATE);
        usuario = String.valueOf(usuarioId);
    }

    private String k(String clave) {
        return usuario + "_" + clave;
    }

    @Override
    public int cuentoEstrellas(int nivel) {
        return prefs.getInt(k("cuento" + nivel + "_estrellas"), 0);
    }

    @Override
    public void guardarCuento(int nivel, int estrellas, int aH, int eH, int aP, int eP) {
        String p = "cuento" + nivel;
        prefs.edit()
                .putInt(k(p + "_estrellas"), estrellas)
                .putInt(k(p + "_aciertosHuecos"), aH)
                .putInt(k(p + "_erroresHuecos"), eH)
                .putInt(k(p + "_aciertosPreguntas"), aP)
                .putInt(k(p + "_erroresPreguntas"), eP)
                .apply();
    }

    @Override
    public boolean palabraDesbloqueada(String id) {
        return palabrasDesbloqueadas().contains(id);
    }

    @Override
    public void desbloquearPalabra(String id) {
        Set<String> s = palabrasDesbloqueadas();
        s.add(id);
        prefs.edit().putStringSet(k("palabras"), s).apply();
    }

    @Override
    public Set<String> palabrasDesbloqueadas() {
        // se copia: no se debe modificar el Set que devuelve SharedPreferences
        return new HashSet<>(prefs.getStringSet(k("palabras"), new HashSet<>()));
    }

    @Override
    public int vecesRepasada(String id) {
        return prefs.getInt(k("repaso_" + id), 0);
    }

    @Override
    public void registrarRepaso(String id) {
        prefs.edit().putInt(k("repaso_" + id), vecesRepasada(id) + 1).apply();
    }
}