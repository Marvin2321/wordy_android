package com.example.wordy;

import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Pinta los 5 nodos del mapa (layout mapa_niveles) según el estado que manda la API:
 * "bloqueado" / "disponible" / "completado"  (igual que cuentosNiveles.html y palabras_nv).
 */
final class MapaNiveles {

    interface AlTocar {
        void nivel(int nivel, String estado);
    }

    private MapaNiveles() {}

    static TextView[] nodos(com.example.wordy.databinding.MapaNivelesBinding m) {
        return new TextView[]{m.nivel1, m.nivel2, m.nivel3, m.nivel4, m.nivel5};
    }

    /** Antes de que responda la API: todos grises y sin acción. */
    static void cargando(TextView[] nodos) {
        for (TextView n : nodos) {
            n.setAlpha(0.4f);
            n.setOnClickListener(null);
        }
    }

    /** @param campo "estado" (cuentos) o "practica"/"imagenes" (palabras) */
    static void pintar(TextView[] nodos, JSONArray niveles, String campo, AlTocar cb) {
        for (int i = 0; i < nodos.length; i++) {
            final int nivel = i + 1;
            String estado = "bloqueado";
            if (niveles != null) {
                for (int k = 0; k < niveles.length(); k++) {
                    JSONObject n = niveles.optJSONObject(k);
                    if (n != null && n.optInt("nivel") == nivel) estado = n.optString(campo, "bloqueado");
                }
            }
            final String est = estado;
            TextView nodo = nodos[i];
            boolean abierto = "disponible".equals(est) || "completado".equals(est);
            nodo.setAlpha(abierto ? 1f : 0.4f);
            nodo.setText("completado".equals(est) ? nivel + "✓" : String.valueOf(nivel));
            nodo.setContentDescription("Nivel " + nivel + ": " + est.replace('_', ' '));
            nodo.setOnClickListener(v -> cb.nivel(nivel, est));
        }
    }

}
