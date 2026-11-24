package com.example.wordy;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cliente HTTP mínimo para la API de Wordy (sin librerías extra).
 * Las llamadas corren en un hilo aparte y el resultado vuelve al hilo principal.
 */
public final class ApiClient {

    // Dominio de tu App Service en Azure. Debe terminar con "/api/".
    public static final String BASE_URL =
            "https://wordy-api-marvin-athheedkfubbh8ez.mexicocentral-01.azurewebsites.net/api/";

    public interface Respuesta {
        void onExito(JSONObject json);
        void onError(String mensaje);
    }

    private static final ExecutorService HILO_RED = Executors.newSingleThreadExecutor();
    private static final Handler HILO_PRINCIPAL = new Handler(Looper.getMainLooper());

    private ApiClient() {}

    public static void post(String ruta, JSONObject cuerpo, Respuesta cb) {
        HILO_RED.execute(() -> {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(BASE_URL + ruta).openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(15_000);
                con.setReadTimeout(60_000); // la 1.ª petición a Azure puede tardar (arranque en frío)
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setRequestProperty("Accept", "application/json");

                try (OutputStream os = con.getOutputStream()) {
                    os.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
                }

                int codigo = con.getResponseCode();
                InputStream is = (codigo >= 400) ? con.getErrorStream() : con.getInputStream();
                String texto = leer(is);
                JSONObject json = texto.isEmpty() ? new JSONObject() : new JSONObject(texto);

                if (codigo >= 200 && codigo < 300 && json.optBoolean("ok", true)) {
                    entregarExito(cb, json);
                } else {
                    entregarError(cb, json.optString("error", "Error del servidor (" + codigo + ")"));
                }
            } catch (IOException e) {
                entregarError(cb, "No se pudo conectar. Revisa tu internet e intenta de nuevo.");
            } catch (JSONException e) {
                entregarError(cb, "Respuesta inesperada del servidor.");
            } finally {
                if (con != null) con.disconnect();
            }
        });
    }

    private static String leer(InputStream is) throws IOException {
        if (is == null) return "";
        try (InputStream in = is; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }
    }

    private static void entregarExito(Respuesta cb, JSONObject json) {
        HILO_PRINCIPAL.post(() -> cb.onExito(json));
    }

    private static void entregarError(Respuesta cb, String mensaje) {
        HILO_PRINCIPAL.post(() -> cb.onError(mensaje));
    }
}