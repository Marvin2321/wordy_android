package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.wordy.databinding.ActivityPracticaPalabrasBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PracticaPalabrasActivity extends BaseWordyActivity {

    public static final String EXTRA_NIVEL = "nivel";
    public static final String EXTRA_REPASO_ID = "repaso_id";

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal, R.drawable.bg_field_purple};

    private ActivityPracticaPalabrasBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private List<Palabra> palabras;
    private int nivel = 1;
    private int indice = 0;
    private boolean repaso = false;

    private Palabra actual;
    private boolean primerIntento = true;
    private boolean bloqueado = false;
    private String silabaElegida;
    private int significadoElegido = -1;
    private int significadoCorrecto;
    private TextView[] chips;
    private final List<TextView> opcionesSignificado = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPracticaPalabrasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        chips = new TextView[]{binding.opcionA, binding.opcionB, binding.opcionC};

        String repasoId = getIntent().getStringExtra(EXTRA_REPASO_ID);
        if (repasoId != null && PalabrasData.porId(repasoId) != null) {
            repaso = true;
            palabras = Collections.singletonList(PalabrasData.porId(repasoId));
        } else {
            nivel = getIntent().getIntExtra(EXTRA_NIVEL, 1);
            palabras = PalabrasData.deNivel(nivel);
            indice = primeraPendiente();
        }

        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
        binding.ivGusanoCabeza.setOnClickListener(v ->
                binding.tvPista.setText(getString(R.string.pista_ejemplo, actual.ejemploOculto())));
        binding.footer.btnRevisar.setOnClickListener(v -> revisar());

        mostrarPalabra();
    }

    private int primeraPendiente() {
        for (int i = 0; i < palabras.size(); i++) {
            if (!juego().palabraDesbloqueada(palabras.get(i).id)) return i;
        }
        return 0;
    }

    private void mostrarPalabra() {
        actual = palabras.get(indice);
        primerIntento = true;
        bloqueado = false;
        silabaElegida = null;
        significadoElegido = -1;

        // ---- Sílabas ----
        if (repaso) {
            binding.cardSilabas.setVisibility(android.view.View.GONE);
            binding.tvSignificado.setText("¿Cuál es el significado correcto de «" + actual.palabra + "»?");
        } else {
            binding.cardSilabas.setVisibility(android.view.View.VISIBLE);
            binding.tvPreguntaSilabas.setText(getString(R.string.pregunta_silabas_fmt,
                    actual.corto, actual.palabraIncompleta()));
            List<String> ops = new ArrayList<>(Arrays.asList(
                    actual.silabaFaltante(), actual.distractores[0], actual.distractores[1]));
            Collections.shuffle(ops);
            for (int i = 0; i < chips.length; i++) {
                chips[i].setText(((char) ('a' + i)) + ")  " + ops.get(i));
                chips[i].setTag(ops.get(i));
                chips[i].setBackgroundResource(FONDOS[i]);
                chips[i].setAlpha(1f);
                chips[i].setOnClickListener(v -> elegirSilaba((TextView) v));
            }
            binding.tvSignificado.setText(R.string.pregunta_significado_titulo);
        }

        // ---- Significado (4 opciones) ----
        List<String> sig = new ArrayList<>();
        sig.add(actual.significado);
        List<Palabra> otras = new ArrayList<>(PalabrasData.todas());
        otras.remove(actual);
        Collections.shuffle(otras);
        for (int i = 0; i < 3; i++) sig.add(otras.get(i).significado);
        Collections.shuffle(sig);
        significadoCorrecto = sig.indexOf(actual.significado);

        binding.llSignificados.removeAllViews();
        opcionesSignificado.clear();
        for (int i = 0; i < sig.size(); i++) {
            TextView t = (TextView) getLayoutInflater()
                    .inflate(R.layout.item_significado, binding.llSignificados, false);
            t.setText(((char) ('A' + i)) + ") " + sig.get(i));
            final int idx = i;
            t.setOnClickListener(v -> elegirSignificado(idx));
            binding.llSignificados.addView(t);
            opcionesSignificado.add(t);
        }

        // ---- Pista ----
        binding.tvPista.setText(R.string.pista_inicial);
        if (actual.imagen != 0) {
            binding.ivPistaImg.setImageResource(actual.imagen);
            binding.ivPistaImg.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.ivPistaImg.setVisibility(android.view.View.GONE);
        }
    }

    private void elegirSilaba(TextView elegido) {
        if (bloqueado) return;
        silabaElegida = (String) elegido.getTag();
        for (TextView c : chips) c.setAlpha(c == elegido ? 1f : 0.5f);
    }

    private void elegirSignificado(int idx) {
        if (bloqueado) return;
        significadoElegido = idx;
        for (int i = 0; i < opcionesSignificado.size(); i++) {
            opcionesSignificado.get(i).setBackgroundResource(i == idx ? R.drawable.bg_opcion_sel : 0);
        }
    }

    private void revisar() {
        if (bloqueado) return;
        if ((!repaso && silabaElegida == null) || significadoElegido < 0) {
            Toast.makeText(this, "Elige una opción en cada pregunta", Toast.LENGTH_SHORT).show();
            return;
        }
        bloqueado = true;

        boolean okSilaba = repaso || silabaElegida.equalsIgnoreCase(actual.silabaFaltante());
        boolean okSignificado = significadoElegido == significadoCorrecto;
        marcarResultado();

        if (okSilaba && okSignificado) {
            registrarAcierto();
        } else {
            primerIntento = false;
            Feedback.error(binding.footer.btnRevisar);
            Toast.makeText(this, "Casi lo logras. Las respuestas correctas están en verde. ¡Inténtalo de nuevo!",
                    Toast.LENGTH_LONG).show();
            handler.postDelayed(() -> {
                mostrarPalabra();
                primerIntento = false;
            }, 2500);
        }
    }

    private void marcarResultado() {
        if (!repaso) {
            for (TextView c : chips) {
                String valor = (String) c.getTag();
                if (valor.equalsIgnoreCase(actual.silabaFaltante())) {
                    c.setBackgroundResource(R.drawable.bg_field_green);
                } else if (valor.equals(silabaElegida)) {
                    c.setBackgroundResource(R.drawable.bg_field_coral);
                }
                c.setAlpha(1f);
            }
        }
        for (int i = 0; i < opcionesSignificado.size(); i++) {
            TextView t = opcionesSignificado.get(i);
            if (i == significadoCorrecto) t.setBackgroundResource(R.drawable.bg_opcion_ok);
            else if (i == significadoElegido) t.setBackgroundResource(R.drawable.bg_opcion_mal);
            else t.setBackgroundResource(0);
        }
    }

    private void registrarAcierto() {
        boolean nueva = !repaso && !juego().palabraDesbloqueada(actual.id);
        int estrellas = 0;
        if (repaso) {
            juego().repasar(actual.id);
        } else if (nueva) {
            juego().desbloquearPalabra(actual.id);
            if (primerIntento) estrellas = 1;
        }
        progreso().registrarActividad(estrellas);
        Feedback.acierto(binding.ivGusanoCabeza);

        StringBuilder msg = new StringBuilder("“" + actual.palabra + "” significa:\n" + actual.significado);
        if (nueva) msg.append("\n\n¡Palabra desbloqueada! La encuentras en “Mis palabras”.");
        if (estrellas > 0) msg.append("\n¡Ganaste +1 estrella!");

        boolean ultima = repaso || indice == palabras.size() - 1;
        new AlertDialog.Builder(this)
                .setTitle("¡Muy bien!")
                .setMessage(msg.toString())
                .setCancelable(false)
                .setPositiveButton(ultima ? "Terminar" : "Siguiente", (d, w) -> siguiente())
                .show();
    }

    private void siguiente() {
        if (repaso) {
            finish();
            return;
        }
        if (indice + 1 < palabras.size()) {
            indice++;
            mostrarPalabra();
            return;
        }
        if (juego().nivelPalabrasCompleto(nivel)) {
            Toast.makeText(this, "¡Nivel " + nivel + " completado!", Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent(this, AprendeNivelesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}