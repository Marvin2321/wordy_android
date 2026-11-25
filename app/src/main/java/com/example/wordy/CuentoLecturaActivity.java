package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordy.databinding.ActivityCuentoLecturaBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CuentoLecturaActivity extends BaseWordyActivity {

    public static final String EXTRA_NIVEL = "nivel";

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal, R.drawable.bg_field_purple};

    private ActivityCuentoLecturaBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private boolean ttsListo = false;

    private Cuento cuento;
    private int escena = 0;
    private String acumulado = "";          // texto de las escenas ya narradas

    private boolean reproduciendo = false;
    private boolean esperandoRespuesta = false;
    private boolean terminado = false;

    private int token = 0;                  // identifica el segmento vigente (ignora los viejos)
    private String segmentoPendiente;
    private Runnable alTerminarSegmento;
    private Runnable simulacion;            // temporizador si no hay voz disponible

    private int aciertosHuecos = 0;
    private int erroresHuecos = 0;
    private boolean huecoConError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCuentoLecturaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cuento = CuentosData.porNivel(getIntent().getIntExtra(EXTRA_NIVEL, 1));
        if (cuento == null) {
            finish();
            return;
        }

        binding.tvSubtitulo.setText(getString(R.string.subtitulo_nivel_cuento, cuento.nivel, cuento.titulo));
        binding.cardOpciones.setVisibility(View.GONE);
        binding.tvCuento.setText("");
        if (cuento.imagenLectura != 0) {
            binding.ivCasas.setImageResource(cuento.imagenLectura);
        } else {
            binding.ivCasas.setVisibility(View.GONE);
        }
        binding.footer.btnRevisar.setAlpha(0.5f);
        binding.footer.btnRevisar.setOnClickListener(v -> irAPreguntas());
        binding.btnPlay.setOnClickListener(v -> alternarReproduccion());

        tts = new TextToSpeech(this, status -> {
            ttsListo = status == TextToSpeech.SUCCESS && configurarVoz();
            if (!ttsListo) {
                Toast.makeText(this, "La voz no está disponible: el cuento avanzará solo.",
                        Toast.LENGTH_SHORT).show();
            }
            iniciarNarracion();
        });
    }

    private boolean configurarVoz() {
        int r = tts.setLanguage(new Locale("es", "MX"));
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            r = tts.setLanguage(new Locale("es"));
        }
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) return false;

        tts.setSpeechRate(0.85f);
        tts.setPitch(1.1f);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) { }
            @Override public void onDone(String utteranceId) { handler.post(() -> finSegmento(utteranceId)); }
            @Override public void onError(String utteranceId) { handler.post(() -> finSegmento(utteranceId)); }
        });
        return true;
    }

    // ===== Narración =====

    private void iniciarNarracion() {
        escena = 0;
        acumulado = "";
        terminado = false;
        aciertosHuecos = 0;
        erroresHuecos = 0;
        reproduciendo = true;
        binding.btnPlay.setImageResource(R.drawable.ic_pausa);
        narrarEscena(0);
    }

    private void narrarEscena(int i) {
        escena = i;
        if (i >= cuento.escenas.size()) {
            terminarCuento();
            return;
        }
        final Cuento.Escena e = cuento.escenas.get(i);
        if (e.hueco == null) {
            mostrar(acumulado, e.antes);
            narrar(e.antes, () -> {
                acumulado += e.antes + " ";
                narrarEscena(i + 1);
            });
        } else {
            mostrar(acumulado, e.antes + "____");
            narrar(e.antes, () -> pedirRespuesta(e));
        }
    }

    private void narrar(String segmento, Runnable alTerminar) {
        segmentoPendiente = segmento;
        alTerminarSegmento = alTerminar;
        if (!reproduciendo) return;     // en pausa: se retoma con Play

        final String id = "seg" + (++token);
        if (ttsListo) {
            tts.speak(segmento, TextToSpeech.QUEUE_FLUSH, null, id);
        } else {
            simulacion = () -> finSegmento(id);
            handler.postDelayed(simulacion, Math.max(1500L, segmento.length() * 55L));
        }
    }

    private void finSegmento(String id) {
        if (!id.equals("seg" + token)) return;      // segmento viejo
        Runnable r = alTerminarSegmento;
        segmentoPendiente = null;
        alTerminarSegmento = null;
        if (r != null) r.run();
    }

    private void alternarReproduccion() {
        if (terminado) {
            binding.footer.btnRevisar.setAlpha(0.5f);
            iniciarNarracion();
            return;
        }
        if (esperandoRespuesta) {
            Toast.makeText(this, "Elige la palabra correcta", Toast.LENGTH_SHORT).show();
            return;
        }
        if (reproduciendo) pausar(); else reanudar();
    }

    private void pausar() {
        reproduciendo = false;
        token++;                                    // invalida callbacks del segmento actual
        if (ttsListo) tts.stop();
        if (simulacion != null) handler.removeCallbacks(simulacion);
        binding.btnPlay.setImageResource(R.drawable.ic_play);
    }

    private void reanudar() {
        reproduciendo = true;
        binding.btnPlay.setImageResource(R.drawable.ic_pausa);
        if (segmentoPendiente != null) {
            narrar(segmentoPendiente, alTerminarSegmento);
        } else if (!esperandoRespuesta) {
            narrarEscena(escena);
        }
    }

    // ===== Huecos =====

    private void pedirRespuesta(Cuento.Escena e) {
        esperandoRespuesta = true;
        huecoConError = false;

        List<String> ops = new ArrayList<>(Arrays.asList(e.hueco.opciones));
        Collections.shuffle(ops);
        TextView[] vs = {binding.opcionA, binding.opcionB, binding.opcionC};
        for (int i = 0; i < vs.length; i++) {
            vs[i].setText(((char) ('a' + i)) + ")    " + ops.get(i));
            vs[i].setTag(ops.get(i));
            vs[i].setBackgroundResource(FONDOS[i]);
            vs[i].setEnabled(true);
            vs[i].setAlpha(1f);
            vs[i].setOnClickListener(v -> responder(e, (TextView) v));
        }
        binding.cardOpciones.setVisibility(View.VISIBLE);
    }

    private void responder(Cuento.Escena e, TextView elegida) {
        if (!esperandoRespuesta) return;
        String palabra = (String) elegida.getTag();

        if (palabra.equalsIgnoreCase(e.hueco.correcta)) {
            esperandoRespuesta = false;
            if (!huecoConError) aciertosHuecos++;
            elegida.setBackgroundResource(R.drawable.bg_field_green);
            Feedback.acierto(elegida);
            Toast.makeText(this, "¡Muy bien!", Toast.LENGTH_SHORT).show();

            handler.postDelayed(() -> {
                binding.cardOpciones.setVisibility(View.GONE);
                String resto = e.hueco.correcta + e.despues;
                mostrar(acumulado + e.antes, resto);
                narrar(resto, () -> {
                    acumulado += e.antes + resto + " ";
                    narrarEscena(escena + 1);
                });
            }, 900);
        } else {
            erroresHuecos++;
            huecoConError = true;
            elegida.setBackgroundResource(R.drawable.bg_field_coral);
            elegida.setEnabled(false);
            elegida.setAlpha(0.6f);
            Feedback.error(elegida);
            Toast.makeText(this, "Casi... ¡Inténtalo otra vez!", Toast.LENGTH_SHORT).show();
        }
    }

    // ===== Final y navegación =====

    private void terminarCuento() {
        terminado = true;
        reproduciendo = false;
        binding.btnPlay.setImageResource(R.drawable.ic_play);
        mostrar(acumulado.trim(), "");
        binding.footer.btnRevisar.setAlpha(1f);
        Toast.makeText(this, "¡Terminaste el cuento! Toca Revisar para responder las preguntas.",
                Toast.LENGTH_LONG).show();
    }

    private void irAPreguntas() {
        if (!terminado) {
            Toast.makeText(this, "Primero termina de escuchar el cuento", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, CuentoPreguntasActivity.class);
        i.putExtra(CuentoPreguntasActivity.EXTRA_NIVEL, cuento.nivel);
        i.putExtra(CuentoPreguntasActivity.EXTRA_ACIERTOS_HUECOS, aciertosHuecos);
        i.putExtra(CuentoPreguntasActivity.EXTRA_ERRORES_HUECOS, erroresHuecos);
        startActivity(i);
    }

    /** Muestra el texto fijo y resalta el segmento que se está narrando. */
    private void mostrar(String fijo, String actual) {
        SpannableStringBuilder sb = new SpannableStringBuilder(fijo);
        int ini = sb.length();
        sb.append(actual);
        if (!actual.isEmpty()) {
            sb.setSpan(new BackgroundColorSpan(0x66FFE48F), ini, sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.tvCuento.setText(sb);
        binding.scrollCuento.post(() -> {
            int y = binding.tvCuento.getBottom() - binding.scrollCuento.getHeight() + dp(60);
            if (y > 0) binding.scrollCuento.smoothScrollTo(0, y);
        });
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}