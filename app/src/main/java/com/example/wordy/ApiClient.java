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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cliente HTTP mínimo para la API de Wordy (sin librerías extra).
 * Es LA MISMA API (repo wordy-backend) que usa la página web, así que todo lo que el niño
 * hace en el celular queda en WordyDB y se ve igual en la web (y al revés).
 *
 * Las llamadas corren en un hilo aparte y el resultado vuelve al hilo principal.
 *   - onExito: el servidor respondió 2xx (OJO: revisa json.optBoolean("ok"), p. ej. /cuentos/validar
 *              responde 200 con ok=false cuando la palabra es incorrecta, igual que en la web).
 *   - onError: no hubo internet o el servidor respondió 4xx/5xx (trae el mensaje "error" de la API).
 */
public final class ApiClient {

    /** Dominio de la API en Azure. Debe terminar con "/api/". (Mismo que script.js de la web.) */
    public static final String BASE_URL =
            "https://wordy-api-marvin-athheedkfubbh8ez.mexicocentral-01.azurewebsites.net/api/";

    /**
     * Dominio de la página web (public_html). Solo se usa si en la BD hay rutas RELATIVAS de imagen o
     * audio (p. ej. "audio/cuentos/nivel1/parte1.mp3"). Las de Cloudinary (https://...) se usan tal cual.
     * Pon aquí la URL de "wordy-web-zamudio" que aparece en el portal de Azure (termina en "/").
     */
    public static final String WEB_BASE = "https://wordy-web-zamudio.azurewebsites.net/";

    public interface Respuesta {
        void onExito(JSONObject json);
        void onError(String mensaje);
    }

    // 2 hilos: permite que una imagen y una llamada a la API no se esperen entre sí.
    private static final ExecutorService HILO_RED = Executors.newFixedThreadPool(2);
    private static final Handler HILO_PRINCIPAL = new Handler(Looper.getMainLooper());

    private ApiClient() {}

    public static void get(String ruta, Respuesta cb) {
        enviar("GET", ruta, null, cb);
    }

    public static void post(String ruta, JSONObject cuerpo, Respuesta cb) {
        enviar("POST", ruta, cuerpo, cb);
    }

    private static void enviar(String metodo, String ruta, JSONObject cuerpo, Respuesta cb) {
        HILO_RED.execute(() -> {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(BASE_URL + ruta).openConnection();
                con.setRequestMethod(metodo);
                con.setConnectTimeout(15_000);
                con.setReadTimeout(60_000); // la 1.ª petición a Azure puede tardar (arranque en frío)
                con.setRequestProperty("Accept", "application/json");

                if (cuerpo != null) {
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                int codigo = con.getResponseCode();
                InputStream is = (codigo >= 400) ? con.getErrorStream() : con.getInputStream();
                String texto = leer(is);
                JSONObject json = texto.isEmpty() ? new JSONObject() : new JSONObject(texto);

                if (codigo >= 200 && codigo < 300) {
                    HILO_PRINCIPAL.post(() -> cb.onExito(json));
                } else {
                    String msg = json.optString("error", "");
                    if (msg.isEmpty()) msg = "Error del servidor (" + codigo + ")";
                    final String m = msg;
                    HILO_PRINCIPAL.post(() -> cb.onError(m));
                }
            } catch (IOException e) {
                HILO_PRINCIPAL.post(() -> cb.onError("No se pudo conectar. Revisa tu internet e intenta de nuevo."));
            } catch (JSONException e) {
                HILO_PRINCIPAL.post(() -> cb.onError("Respuesta inesperada del servidor."));
            } finally {
                if (con != null) con.disconnect();
            }
        });
    }

    static String leer(InputStream is) throws IOException {
        if (is == null) return "";
        try (InputStream in = is; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }
    }

    // ===== utilidades =====

    /** Codifica un valor para la URL (?idUsuario=...). */
    public static String q(String valor) {
        try {
            return URLEncoder.encode(valor == null ? "" : valor, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    /** Convierte una ruta de la BD en URL completa (null si viene vacía). */
    public static String urlCompleta(String ruta) {
        if (ruta == null) return null;
        String r = ruta.trim();
        if (r.isEmpty() || r.equals("null")) return null;
        if (r.startsWith("http://") || r.startsWith("https://")) return r;
        while (r.startsWith("/")) r = r.substring(1);
        return WEB_BASE + r;
    }

    /** Fecha LOCAL del celular en formato AAAA-MM-DD (igual que fechaLocal() de app.js). */
    public static String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    /** optString que convierte el null de JSON en "" (optString devolvería el texto "null"). */
    public static String txt(JSONObject o, String clave) {
        if (o == null || o.isNull(clave)) return "";
        return o.optString(clave, "");
    }

    /** Ejecuta trabajo de red en segundo plano (lo usa ImagenUrl). */
    static void enSegundoPlano(Runnable r) {
        HILO_RED.execute(r);
    }

    static void enPrincipal(Runnable r) {
        HILO_PRINCIPAL.post(r);
    }
}
