package com.example.wordy;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.wordy.databinding.ActivityCuentoLecturaBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Lector del cuento (cuentoLeer.html). MISMA lógica que cuentosModulo.js:
 *   1) GET  /api/cuentos/nivel/{n}?idUsuario=   cuento + preguntas (SIN respuestas) + ticket
 *   2) narra escena por escena (mp3 de la BD o, si no hay, la voz del celular)
 *   3) se detiene en cada hueco y valida la palabra con POST /api/cuentos/validar
 *   4) al terminar, "Revisar" abre la trivia (CuentoPreguntasActivity) que guarda el avance.
 */
public class CuentoLecturaActivity extends BaseWordyActivity {

    public static final String EXTRA_NIVEL = "nivel";

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal,
            R.drawable.bg_field_purple, R.drawable.bg_field_pink};
    private static final String PUNTUACION = ".,;:!?”»)…";

    private ActivityCuentoLecturaBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private boolean ttsListo = false;
    private MediaPlayer player;
    private String srcActual = "";

    // ----- datos que manda la API -----
    private JSONObject cuento;
    private JSONArray escenas;
    private JSONArray preguntas;
    private String ticket;

    // ----- estado (igual que "estado" en cuentosModulo.js) -----
    private int escena = 0;
    private boolean reproduciendo = false;
    private boolean esperandoRespuesta = false;
    private boolean terminado = false;
    private boolean enviando = false;
    private int token = 0;                     // invalida eventos viejos (pausa / cambio de escena)
    private String[] palabraResuelta;          // escena -> palabra correcta (la devuelve el servidor)
    private int[] fallosHueco;

    // ----- estadística que se manda a /finalizar -----
    private int huecosPrimerIntento = 0;       // hAciertos
    private int toques = 0;                    // hIntentos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityCuentoLecturaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardOpciones.setVisibility(View.GONE);
        binding.tvCuento.setText("Cargando el cuento…");
        binding.ivCasas.setVisibility(View.GONE);
        binding.footer.btnRevisar.setAlpha(0.5f);
        binding.footer.btnRevisar.setOnClickListener(v -> irAPreguntas());
        binding.btnPlay.setOnClickListener(v -> alternarReproduccion());

        int nivel = getIntent().getIntExtra(EXTRA_NIVEL, 1);
        tts = new TextToSpeech(this, status -> ttsListo = status == TextToSpeech.SUCCESS && configurarVoz());
        cargarCuento(nivel);
    }

    // ================================================================ CARGA
    private void cargarCuento(int nivel) {
        ApiClient.get("cuentos/nivel/" + nivel + "?idUsuario=" + ApiClient.q(idUsuario()), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                JSONObject c = json.optJSONObject("cuento");
                if (!json.optBoolean("ok", false) || c == null || c.optJSONArray("escenas") == null
                        || c.optJSONArray("escenas").length() == 0) {
                    error("No se pudo cargar el cuento, intenta de nuevo.");
                    return;
                }
                cuento = c;
                escenas = c.optJSONArray("escenas");
                preguntas = json.optJSONArray("preguntas");
                ticket = ApiClient.txt(json, "ticket");
                palabraResuelta = new String[escenas.length()];
                fallosHueco = new int[escenas.length()];

                binding.tvSubtitulo.setText(ApiClient.txt(c, "tituloNivel"));
                ImagenUrl.cargar(binding.ivCasas, ApiClient.txt(c, "ilustracionInicial"), 0);
                String avisoApi = ApiClient.txt(json, "aviso");
                if (!avisoApi.isEmpty()) aviso(avisoApi);
                pintarTexto(-1);
                Toast.makeText(CuentoLecturaActivity.this, "Pulsa ▶ para escuchar el cuento.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                error(mensaje);
            }
        });
    }

    private void error(String texto) {
        binding.tvCuento.setText(texto);
        aviso(texto);
    }

    private boolean configurarVoz() {
        int r = tts.setLanguage(new Locale("es", "MX"));
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            r = tts.setLanguage(new Locale("es"));
        }
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) return false;
        tts.setSpeechRate(0.9f);
        tts.setPitch(1.05f);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) { }
            @Override public void onDone(String id) { handler.post(() -> finDeVoz(id)); }
            @Override public void onError(String id) { handler.post(() -> finDeVoz(id)); }
        });
        return true;
    }

    // ================================================================ TEXTO
    private JSONObject esc(int i) {
        return escenas.optJSONObject(i);
    }

    private JSONObject hueco(int i) {
        JSONObject e = esc(i);
        return e == null ? null : e.optJSONObject("hueco");
    }

    /** Dibuja todo el cuento: escenas + huecos (***** o la palabra ya resuelta) y resalta la escena actual. */
    private void pintarTexto(int actual) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int resaltadoIni = -1, resaltadoFin = -1;
        for (int i = 0; i < escenas.length(); i++) {
            String t = ApiClient.txt(esc(i), "texto");
            if (i > 0 && !t.isEmpty() && PUNTUACION.indexOf(t.charAt(0)) < 0) sb.append(' ');
            int ini = sb.length();
            boolean conHueco = hueco(i) != null;
            String marca = palabraResuelta[i] != null ? palabraResuelta[i]
                    : (esperandoRespuesta && i == escena ? "_____" : "*****");
            int blanco = conHueco ? t.indexOf("____") : -1;
            int hi;
            if (blanco >= 0) {                       // el texto ya trae el espacio "____": se rellena ahí
                sb.append(t.substring(0, blanco));
                hi = sb.length();
                sb.append(marca);
                int hf = sb.length();
                int fin = blanco;
                while (fin < t.length() && t.charAt(fin) == '_') fin++;
                sb.append(t.substring(fin));
                colorear(sb, hi, hf, palabraResuelta[i] != null);
            } else {
                sb.append(t);
                if (conHueco) {                      // como en la web: el hueco va después de la escena
                    sb.append(' ');
                    hi = sb.length();
                    sb.append(marca);
                    colorear(sb, hi, sb.length(), palabraResuelta[i] != null);
                }
            }
            if (i == actual) { resaltadoIni = ini; resaltadoFin = sb.length(); }
        }
        if (resaltadoIni >= 0 && resaltadoFin > resaltadoIni) {
            sb.setSpan(new BackgroundColorSpan(0x66FFE48F), resaltadoIni, resaltadoFin,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.tvCuento.setText(sb);
        if (resaltadoIni >= 0) {
            final int pos = resaltadoIni;
            binding.tvCuento.post(() -> {
                if (binding.tvCuento.getLayout() == null) return;
                int linea = binding.tvCuento.getLayout().getLineForOffset(pos);
                int y = binding.tvCuento.getLayout().getLineTop(linea) - dp(40);
                binding.scrollCuento.smoothScrollTo(0, Math.max(0, y));
            });
        }
    }

    private void colorear(SpannableStringBuilder sb, int ini, int fin, boolean resuelto) {
        int color = resuelto ? R.color.wordy_teal_dark : R.color.wordy_coral_dark;
        sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, color)), ini, fin,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // ================================================================ NARRACIÓN
    private void alternarReproduccion() {
        if (escenas == null) return;
        if (terminado) {
            Toast.makeText(this, "Ya terminaste: toca Revisar para responder las preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (esperandoRespuesta) {
            Toast.makeText(this, "Elige la palabra correcta", Toast.LENGTH_SHORT).show();
            return;
        }
        if (reproduciendo) pausar(); else hablar(escena);
    }

    private void detenerTodo() {
        token++;
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            try { if (player.isPlaying()) player.pause(); } catch (Exception ignored) { }
        }
        if (ttsListo) tts.stop();
        reproduciendo = false;
    }

    private void pausar() {
        detenerTodo();
        binding.btnPlay.setImageResource(R.drawable.ic_play);
    }

    private void hablar(int i) {
        detenerTodo();
        escena = i;
        reproduciendo = true;
        binding.btnPlay.setImageResource(R.drawable.ic_pausa);
        pintarTexto(i);
        mostrarIlustracion(i);
        String audio = ApiClient.urlCompleta(ApiClient.txt(esc(i), "audio"));
        if (audio == null) hablarConVoz(i, token); else reproducirAudio(i, audio, token, 0);
    }

    /** Forma A: un mp3 por escena. Forma B: un mp3 por cuento + tramo inicio/fin (segundos). */
    private void reproducirAudio(int i, String url, int t, int intento) {
        JSONObject e = esc(i);
        final boolean tramo = e.has("inicio") && e.has("fin") && !e.isNull("inicio") && !e.isNull("fin");
        final int inicioMs = (int) Math.round(e.optDouble("inicio", 0) * 1000);
        final int finMs = (int) Math.round(e.optDouble("fin", 0) * 1000);

        Runnable arrancar = () -> {
            if (t != token) return;
            try {
                if (tramo) player.seekTo(inicioMs);
                player.start();
                if (tramo) vigilarTramo(i, t, finMs);
            } catch (Exception ex) {
                falloAudio(i, url, t, intento);
            }
        };

        try {
            if (player != null && url.equals(srcActual)) {   // mismo mp3 ya cargado: solo salta al tramo
                player.setOnCompletionListener(mp -> { if (t == token) finSegmento(i); });
                if (!tramo) player.seekTo(0);
                arrancar.run();
                return;
            }
            liberarPlayer();
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build());
            player.setDataSource(url);
            player.setOnPreparedListener(mp -> { srcActual = url; arrancar.run(); });
            player.setOnCompletionListener(mp -> { if (t == token) finSegmento(i); });
            player.setOnErrorListener((mp, w, x) -> { falloAudio(i, url, t, intento); return true; });
            player.prepareAsync();
        } catch (Exception ex) {
            falloAudio(i, url, t, intento);
        }
    }

    private void vigilarTramo(int i, int t, int finMs) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (t != token || player == null) return;
                try {
                    if (player.getCurrentPosition() >= finMs - 30) {
                        player.pause();
                        finSegmento(i);
                        return;
                    }
                } catch (Exception ignored) { }
                handler.postDelayed(this, 80);
            }
        }, 80);
    }

    /** Si el audio falla: 1 reintento y luego la voz del celular (como en la web). */
    private void falloAudio(int i, String url, int t, int intento) {
        if (t != token) return;
        srcActual = "";
        liberarPlayer();
        if (intento < 1) {
            Toast.makeText(this, "No pudimos cargar el audio. Reintentando…", Toast.LENGTH_SHORT).show();
            handler.postDelayed(() -> { if (t == token) reproducirAudio(i, url, t, intento + 1); }, 900);
        } else {
            Toast.makeText(this, "No encontramos el audio. Usaremos la voz del celular.", Toast.LENGTH_SHORT).show();
            hablarConVoz(i, t);
        }
    }

    /** Texto que lee la voz: la palabra del hueco anterior + la escena (textoParaVoz en la web). */
    private String textoParaVoz(int i) {
        String previa = (i > 0 && palabraResuelta[i - 1] != null) ? palabraResuelta[i - 1] : "";
        String t = ApiClient.txt(esc(i), "texto").replace("____", "");
        if (previa.isEmpty()) return t;
        return previa + (!t.isEmpty() && PUNTUACION.indexOf(t.charAt(0)) >= 0 ? "" : " ") + t;
    }

    private void hablarConVoz(int i, int t) {
        if (!ttsListo) {                     // sin voz: lectura silenciosa con tiempo de lectura
            long ms = Math.max(2500L, textoParaVoz(i).length() * 55L);
            handler.postDelayed(() -> { if (t == token) finSegmento(i); }, ms);
            return;
        }
        tts.speak(textoParaVoz(i), TextToSpeech.QUEUE_FLUSH, null, "seg" + t + "_" + i);
    }

    private void finDeVoz(String id) {
        if (id == null || !id.startsWith("seg" + token + "_")) return;   // evento viejo
        int i = Integer.parseInt(id.substring(id.indexOf('_') + 1));
        finSegmento(i);
    }

    private void finSegmento(int i) {
        reproduciendo = false;
        if (hueco(i) != null && palabraResuelta[i] == null) {
            pedirPalabra(i);
            return;
        }
        siguiente(i);
    }

    private void siguiente(int i) {
        if (i + 1 >= escenas.length()) {
            terminarCuento();
            return;
        }
        hablar(i + 1);
    }

    private void mostrarIlustracion(int i) {
        String ruta = ApiClient.txt(esc(i), "ilustracion");
        if (!ruta.isEmpty()) ImagenUrl.cargar(binding.ivCasas, ruta, 0);
    }

    // ================================================================ HUECOS
    private void pedirPalabra(int i) {
        esperandoRespuesta = true;
        binding.btnPlay.setImageResource(R.drawable.ic_play);
        pintarTexto(i);

        JSONObject h = hueco(i);
        JSONArray ops = h.optJSONArray("opciones");
        LinearLayout card = binding.cardOpciones;
        card.removeAllViews();

        TextView pregunta = new TextView(this);
        pregunta.setText("¿Qué palabra completa la historia?\n" + ApiClient.txt(h, "antes") + " ____ " + ApiClient.txt(h, "despues"));
        pregunta.setTextColor(ContextCompat.getColor(this, R.color.wordy_navy));
        pregunta.setTextSize(13);
        pregunta.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.varela_normal),
                android.graphics.Typeface.BOLD);
        card.addView(pregunta);

        for (int k = 0; ops != null && k < ops.length(); k++) {
            final String palabra = ops.optString(k);
            TextView b = (TextView) getLayoutInflater().inflate(R.layout.item_opcion, card, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(42));
            lp.topMargin = dp(8);
            b.setLayoutParams(lp);
            b.setText(((char) ('a' + k)) + ")    " + palabra);
            b.setBackgroundResource(FONDOS[k % FONDOS.length]);
            b.setOnClickListener(v -> elegir(i, palabra, b));
            card.addView(b);
        }
        card.setVisibility(View.VISIBLE);
    }

    /** POST /api/cuentos/validar {idHueco, respuesta} -> {ok, palabra}. La correcta nunca llega antes. */
    private void elegir(int i, String palabra, TextView boton) {
        if (!esperandoRespuesta || enviando) return;
        enviando = true;
        toques++;
        final int previos = fallosHueco[i];

        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idHueco", hueco(i).optInt("id"));
            cuerpo.put("respuesta", palabra);
        } catch (JSONException e) {
            enviando = false;
            return;
        }
        ApiClient.post("cuentos/validar", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                enviando = false;
                if (cerrada()) return;
                if (json.optBoolean("ok", false)) {
                    if (previos == 0) huecosPrimerIntento++;
                    boton.setBackgroundResource(R.drawable.bg_field_green);
                    Feedback.acierto(boton);
                    Toast.makeText(CuentoLecturaActivity.this, "¡Muy bien!", Toast.LENGTH_SHORT).show();
                    resolver(i, ApiClient.txt(json, "palabra").isEmpty() ? palabra : ApiClient.txt(json, "palabra"));
                } else {
                    fallosHueco[i] = previos + 1;
                    boton.setBackgroundResource(R.drawable.bg_field_coral);
                    boton.setEnabled(false);
                    boton.setAlpha(0.6f);
                    Feedback.error(boton);
                    Toast.makeText(CuentoLecturaActivity.this, "¡Casi! Inténtalo otra vez, tú puedes.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String mensaje) {
                enviando = false;
                toques--;            // no cuenta un toque que no llegó al servidor
                if (cerrada()) return;
                aviso(mensaje + " Intenta de nuevo.");
            }
        });
    }

    private void resolver(int i, String palabra) {
        palabraResuelta[i] = palabra;
        esperandoRespuesta = false;
        pintarTexto(i);
        handler.postDelayed(() -> {
            if (cerrada()) return;
            binding.cardOpciones.setVisibility(View.GONE);
            siguiente(i);     // la siguiente escena empieza diciendo esta palabra
        }, 900);
    }

    // ================================================================ FINAL
    private void terminarCuento() {
        detenerTodo();
        terminado = true;
        binding.btnPlay.setImageResource(R.drawable.ic_play);
        pintarTexto(-1);
        binding.footer.btnRevisar.setAlpha(1f);
        Toast.makeText(this, "¡Terminaste el cuento! Toca Revisar para responder las preguntas.",
                Toast.LENGTH_LONG).show();
    }

    private void irAPreguntas() {
        if (escenas == null) return;
        if (!terminado) {
            Toast.makeText(this, "Primero escucha el cuento completo para poder revisar.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, CuentoPreguntasActivity.class);
        i.putExtra(CuentoPreguntasActivity.EXTRA_ID_CUENTO, cuento.optInt("id"));
        i.putExtra(CuentoPreguntasActivity.EXTRA_TITULO, ApiClient.txt(cuento, "tituloNivel"));
        i.putExtra(CuentoPreguntasActivity.EXTRA_PORTADA, ApiClient.txt(cuento, "portada"));
        i.putExtra(CuentoPreguntasActivity.EXTRA_PREGUNTAS, preguntas == null ? "[]" : preguntas.toString());
        i.putExtra(CuentoPreguntasActivity.EXTRA_TICKET, ticket);
        i.putExtra(CuentoPreguntasActivity.EXTRA_H_ACIERTOS, huecosPrimerIntento);
        i.putExtra(CuentoPreguntasActivity.EXTRA_H_INTENTOS, toques);
        startActivity(i);
        finish();     // como en la web: el lector se reemplaza por la trivia
    }

    private void liberarPlayer() {
        if (player != null) {
            try { player.release(); } catch (Exception ignored) { }
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (reproduciendo) pausar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        liberarPlayer();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
