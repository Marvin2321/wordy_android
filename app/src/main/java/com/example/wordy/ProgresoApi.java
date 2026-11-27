package com.example.wordy;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Racha y estrellas del niño, leídas de WordyDB (PERFIL_NINO + PROGRESO_CUENTO) con /api/nino.
 * Reemplaza a ProgresoLocal/ProgresoManager (que guardaban en el celular y no se veían en la web).
 */
public final class ProgresoApi {

    public interface AlTerminar {
        void listo(int racha, int estrellas);
    }

    private ProgresoApi() {}

    /** GET /api/nino/resumen -> {estrellas, racha}. Si falla, devuelve lo último guardado. */
    public static void cargarResumen(Context c, AlTerminar cb) {
        Context app = c.getApplicationContext();
        String id = SesionManager.usuarioId(app);
        if (id == null) {
            cb.listo(0, 0);
            return;
        }
        ApiClient.get("nino/resumen?idUsuario=" + ApiClient.q(id) + "&hoy=" + ApiClient.hoy(),
                new ApiClient.Respuesta() {
                    @Override
                    public void onExito(JSONObject json) {
                        int racha = json.optInt("racha", 0);
                        int estrellas = json.optInt("estrellas", 0);
                        SesionManager.guardarResumen(app, racha, estrellas);
                        cb.listo(racha, estrellas);
                    }

                    @Override
                    public void onError(String mensaje) {
                        cb.listo(SesionManager.racha(app), SesionManager.estrellas(app));
                    }
                });
    }

    /**
     * POST /api/nino/dia: marca HOY como día jugado (igual que registrarDiaJugado() de app.js).
     * No bloquea al niño: si falla, simplemente no suma el día.
     */
    public static void registrarDia(Context c) {
        Context app = c.getApplicationContext();
        String id = SesionManager.usuarioId(app);
        if (id == null) return;
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idUsuario", id);
            cuerpo.put("hoy", ApiClient.hoy());
        } catch (JSONException e) {
            return;
        }
        ApiClient.post("nino/dia", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (json.optBoolean("perfilNino", false)) {
                    SesionManager.guardarResumen(app, json.optInt("racha", 0), SesionManager.estrellas(app));
                }
            }

            @Override
            public void onError(String mensaje) { /* sin internet: no pasa nada */ }
        });
    }
}
