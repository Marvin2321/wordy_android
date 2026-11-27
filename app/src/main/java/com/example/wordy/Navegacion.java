package com.example.wordy;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Navegacion {

    private Navegacion() {}

    public static void irAHome(Activity a) {
        Intent i = new Intent(a, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
        a.finish();
    }

    /**
     * Botón "¡Continuar aventura!": pregunta a la API qué sigue.
     *   1) primer cuento "disponible" (no completado) en /api/cuentos/mapa
     *   2) si no, primer nivel "disponible" de Aprende palabras
     *   3) si ya terminó todo, abre Aprende palabras
     */
    public static void continuarAventura(Activity a, Runnable alTerminar) {
        String id = ApiClient.q(SesionManager.usuarioId(a));
        ApiClient.get("cuentos/mapa?idUsuario=" + id, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (a.isFinishing()) return;
                int nivel = primerDisponible(json.optJSONArray("niveles"), "estado");
                if (nivel > 0) {
                    alTerminar.run();
                    Intent i = new Intent(a, CuentoLecturaActivity.class);
                    i.putExtra(CuentoLecturaActivity.EXTRA_NIVEL, nivel);
                    a.startActivity(i);
                } else {
                    siguientePalabras(a, id, alTerminar);
                }
            }

            @Override
            public void onError(String mensaje) {
                if (a.isFinishing()) return;
                siguientePalabras(a, id, alTerminar);
            }
        });
    }

    private static void siguientePalabras(Activity a, String id, Runnable alTerminar) {
        ApiClient.get("palabras/mapa?idUsuario=" + id, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (a.isFinishing()) return;
                alTerminar.run();
                int nivel = primerDisponible(json.optJSONArray("niveles"), "practica");
                Intent i;
                if (nivel > 0) {
                    i = new Intent(a, PracticaPalabrasActivity.class);
                    i.putExtra(PracticaPalabrasActivity.EXTRA_NIVEL, nivel);
                } else {
                    i = new Intent(a, AprendePalabrasActivity.class);
                }
                a.startActivity(i);
            }

            @Override
            public void onError(String mensaje) {
                if (a.isFinishing()) return;
                alTerminar.run();
                android.widget.Toast.makeText(a, mensaje, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Primer nivel cuyo campo vale "disponible" (0 si ninguno). */
    static int primerDisponible(JSONArray niveles, String campo) {
        if (niveles == null) return 0;
        for (int k = 0; k < niveles.length(); k++) {
            JSONObject n = niveles.optJSONObject(k);
            if (n != null && "disponible".equals(n.optString(campo))) return n.optInt("nivel");
        }
        return 0;
    }
}
