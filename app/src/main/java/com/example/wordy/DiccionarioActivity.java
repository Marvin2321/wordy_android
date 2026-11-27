package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.wordy.databinding.ActivityDiccionarioBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * "Diccionario del Gusanito" (CU 9): GET /api/palabras/mis-palabras.
 * Solo trae el contenido de las palabras YA desbloqueadas (PALABRA_DESBLOQUEADA), igual que la web.
 * Tocar una tarjeta abre el repaso; tocar el ícono la lee en voz alta.
 */
public class DiccionarioActivity extends BaseWordyActivity {

    private ActivityDiccionarioBinding binding;
    private DiccionarioAdapter adapter;
    private TextView[] circulos;
    private int nivelSel = 0;
    private JSONArray todas = new JSONArray();
    private TextToSpeech tts;
    private boolean ttsListo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityDiccionarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        circulos = new TextView[]{binding.nivelD1, binding.nivelD2, binding.nivelD3,
                binding.nivelD4, binding.nivelD5};
        for (int i = 0; i < circulos.length; i++) {
            final int nivel = i + 1;
            circulos[i].setOnClickListener(v -> mostrarNivel(nivel));
        }

        adapter = new DiccionarioAdapter(new DiccionarioAdapter.Acciones() {
            @Override
            public void repasar(JSONObject p) {
                abrirRepaso(p);
            }

            @Override
            public void escuchar(JSONObject p) {
                hablar(ApiClient.txt(p, "palabra") + ". " + ApiClient.txt(p, "significado"));
            }
        });
        GridLayoutManager lm = new GridLayoutManager(this, 2);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.esVacio(position) ? 2 : 1;
            }
        });
        binding.rvPalabras.setLayoutManager(lm);
        binding.rvPalabras.setAdapter(adapter);

        tts = new TextToSpeech(this, s -> {
            if (s == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(new Locale("es", "MX"));
                ttsListo = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) cargar();     // al volver de un repaso se actualiza "Repasada N veces"
    }

    private void cargar() {
        ApiClient.get("palabras/mis-palabras?idUsuario=" + ApiClient.q(idUsuario()), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                if (!json.optBoolean("ok", false)) {
                    aviso(json.optString("error", "No pudimos cargar tus palabras."));
                    return;
                }
                todas = json.optJSONArray("palabras") == null ? new JSONArray() : json.optJSONArray("palabras");
                mostrarNivel(nivelSel > 0 ? nivelSel : nivelInicial());
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                adapter.setPalabras(new ArrayList<>(), mensaje);
            }
        });
    }

    /** El nivel más alto que ya tiene palabras desbloqueadas (o el 1). */
    private int nivelInicial() {
        int mejor = 1;
        for (int k = 0; k < todas.length(); k++) {
            JSONObject p = todas.optJSONObject(k);
            if (p != null && p.optBoolean("desbloqueada")) mejor = Math.max(mejor, p.optInt("nivel", 1));
        }
        return mejor;
    }

    private void mostrarNivel(int nivel) {
        nivelSel = nivel;
        binding.tvSubtitulo.setText(getString(R.string.subtitulo_diccionario_fmt, nivel));
        for (int i = 0; i < circulos.length; i++) {
            circulos[i].setAlpha(i + 1 == nivel ? 1f : 0.55f);
        }
        List<JSONObject> lista = new ArrayList<>();
        int delNivel = 0;
        for (int k = 0; k < todas.length(); k++) {
            JSONObject p = todas.optJSONObject(k);
            if (p == null || p.optInt("nivel") != nivel) continue;
            delNivel++;
            if (p.optBoolean("desbloqueada")) lista.add(p);
        }
        String vacio = delNivel == 0 ? "Este nivel todavía no tiene palabras."
                : "Has descubierto 0 de " + delNivel + " palabras de este nivel. ¡Juega para desbloquearlas!";
        adapter.setPalabras(lista, vacio);
    }

    /** "Repasar": actividad de 4 opciones solo con esa palabra (GET /api/palabras/repaso/{id}). */
    private void abrirRepaso(JSONObject p) {
        if (!p.optBoolean("puedeRepasar", false)) {
            Toast.makeText(this, "Esta palabra todavía no tiene actividad de repaso.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, PracticaPalabrasActivity.class);
        i.putExtra(PracticaPalabrasActivity.EXTRA_REPASO_ID, p.optInt("idPalabra"));
        startActivity(i);
    }

    private void hablar(String texto) {
        if (!ttsListo) {
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
            return;
        }
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "dic");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
