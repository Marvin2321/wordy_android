package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.wordy.databinding.ActivityCuentoPreguntasBinding;

import java.util.ArrayList;
import java.util.List;

public class CuentoPreguntasActivity extends BaseWordyActivity {

    public static final String EXTRA_NIVEL = "nivel";
    public static final String EXTRA_ACIERTOS_HUECOS = "aciertos_huecos";
    public static final String EXTRA_ERRORES_HUECOS = "errores_huecos";

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal, R.drawable.bg_field_purple};

    private ActivityCuentoPreguntasBinding binding;
    private Cuento cuento;
    private int[] elegidas;
    private final List<List<TextView>> chips = new ArrayList<>();
    private boolean revisado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCuentoPreguntasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cuento = CuentosData.porNivel(getIntent().getIntExtra(EXTRA_NIVEL, 1));
        if (cuento == null) {
            finish();
            return;
        }
        binding.tvSubtitulo.setText(getString(R.string.subtitulo_nivel_cuento, cuento.nivel, cuento.titulo));
        elegidas = new int[cuento.preguntas.size()];
        java.util.Arrays.fill(elegidas, -1);
        construirPreguntas();
        binding.footer.btnRevisar.setOnClickListener(v -> revisar());
    }

    private void construirPreguntas() {
        for (int q = 0; q < cuento.preguntas.size(); q++) {
            Cuento.Pregunta p = cuento.preguntas.get(q);
            View bloque = getLayoutInflater().inflate(R.layout.item_pregunta, binding.llPreguntas, false);
            ((TextView) bloque.findViewById(R.id.tvEnunciado)).setText((q + 1) + ". " + p.enunciado);

            LinearLayout cont = bloque.findViewById(R.id.llOpciones);
            boolean fila = sonCortas(p);
            cont.setOrientation(fila ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

            List<TextView> lista = new ArrayList<>();
            for (int o = 0; o < p.opciones.length; o++) {
                TextView chip = (TextView) getLayoutInflater().inflate(R.layout.item_opcion, cont, false);
                chip.setText(((char) ('a' + o)) + ")  " + p.opciones[o]);
                chip.setBackgroundResource(FONDOS[o % 3]);
                LinearLayout.LayoutParams lp = fila
                        ? new LinearLayout.LayoutParams(0, dp(42), 1f)
                        : new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
                if (fila) lp.setMarginEnd(dp(8)); else lp.topMargin = dp(8);
                chip.setLayoutParams(lp);
                final int qi = q, oi = o;
                chip.setOnClickListener(v -> elegir(qi, oi));
                cont.addView(chip);
                lista.add(chip);
            }
            chips.add(lista);
            binding.llPreguntas.addView(bloque);

            if (q == 0 && cuento.imagenPreguntas != 0) {
                ImageView iv = new ImageView(this);
                iv.setImageResource(cuento.imagenPreguntas);
                iv.setAdjustViewBounds(true);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(130));
                lp.topMargin = dp(10);
                binding.llPreguntas.addView(iv, lp);
            }
        }
    }

    private boolean sonCortas(Cuento.Pregunta p) {
        for (String o : p.opciones) if (o.length() > 10) return false;
        return true;
    }

    private void elegir(int q, int o) {
        if (revisado) return;
        elegidas[q] = o;
        List<TextView> lista = chips.get(q);
        for (int i = 0; i < lista.size(); i++) lista.get(i).setAlpha(i == o ? 1f : 0.5f);
    }

    private void revisar() {
        if (revisado) {          // segunda pulsación: volver al mapa
            volverANiveles();
            return;
        }
        for (int r : elegidas) {
            if (r < 0) {
                Toast.makeText(this, "Responde todas las preguntas", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        revisado = true;

        int aciertos = 0;
        View ultimaBuena = null, ultimaMala = null;
        for (int q = 0; q < cuento.preguntas.size(); q++) {
            Cuento.Pregunta p = cuento.preguntas.get(q);
            List<TextView> lista = chips.get(q);
            for (int o = 0; o < lista.size(); o++) {
                TextView c = lista.get(o);
                c.setAlpha(1f);
                if (o == p.correcta) {
                    c.setBackgroundResource(R.drawable.bg_field_green);
                } else if (o == elegidas[q]) {
                    c.setBackgroundResource(R.drawable.bg_field_coral);
                    ultimaMala = c;
                }
            }
            if (elegidas[q] == p.correcta) {
                aciertos++;
                ultimaBuena = lista.get(elegidas[q]);
            }
        }
        int total = cuento.preguntas.size();
        if (aciertos == total && ultimaBuena != null) Feedback.acierto(ultimaBuena);
        else if (ultimaMala != null) Feedback.error(ultimaMala);

        int aH = getIntent().getIntExtra(EXTRA_ACIERTOS_HUECOS, 0);
        int eH = getIntent().getIntExtra(EXTRA_ERRORES_HUECOS, 0);
        int nuevas = juego().guardarCuento(cuento.nivel, aH, eH, cuento.totalHuecos(),
                aciertos, total - aciertos, total);
        progreso().registrarActividad(nuevas);

        StringBuilder msg = new StringBuilder("Respondiste bien " + aciertos + " de " + total + ".");
        if (aciertos < total) msg.append("\nLas respuestas correctas están en verde.");
        msg.append("\n\nEstrellas del cuento: ").append(juego().estrellasCuento(cuento.nivel)).append(" de 3");
        if (nuevas > 0) msg.append("\n¡Ganaste +").append(nuevas).append(nuevas == 1 ? " estrella!" : " estrellas!");

        new AlertDialog.Builder(this)
                .setTitle(aciertos == total ? "¡Excelente!" : "¡Buen intento!")
                .setMessage(msg.toString())
                .setCancelable(false)
                .setNegativeButton("Ver respuestas", null)
                .setPositiveButton("Continuar", (d, w) -> volverANiveles())
                .show();
    }

    private void volverANiveles() {
        Intent i = new Intent(this, CuentaCuentosNivelesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}