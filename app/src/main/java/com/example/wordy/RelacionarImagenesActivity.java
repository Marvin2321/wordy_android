package com.example.wordy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.wordy.databinding.ActivityRelacionarImagenesBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RelacionarImagenesActivity extends BaseWordyActivity {

    private static final String[] NOMBRES = {"Baya", "Ajeno", "Fenicio", "Pueril", "Gazapo"};
    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal, R.drawable.bg_field_purple,
            R.drawable.bg_field_pink, R.drawable.bg_field_coral};

    private ActivityRelacionarImagenesBinding binding;
    private ImageView[] imagenes;   // imagen i corresponde a NOMBRES[i]
    private TextView[] chips;       // cada chip lleva en su tag el índice de la palabra
    private Palabra[] palabras;

    private boolean[] resuelta;
    private int[] errores;
    private int chipSel = -1;
    private int imgSel = -1;
    private int resueltas = 0;
    private int estrellasRonda = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRelacionarImagenesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagenes = new ImageView[]{binding.ivBaya, binding.ivAjeno, binding.ivFenicio,
                binding.ivPueril, binding.ivGazapo};
        chips = new TextView[]{binding.palabraBaya, binding.palabraGazapo, binding.palabraAjeno,
                binding.palabraPueril, binding.palabraFenicio};
        palabras = new Palabra[NOMBRES.length];
        for (int i = 0; i < NOMBRES.length; i++) palabras[i] = PalabrasData.porNombre(NOMBRES[i]);

        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new android.content.Intent(this, DiccionarioActivity.class)));
        binding.footer.btnRevisar.setOnClickListener(v ->
                Toast.makeText(this, "Llevas " + resueltas + " de " + NOMBRES.length
                        + " palabras relacionadas", Toast.LENGTH_SHORT).show());

        iniciarRonda();
    }

    private void iniciarRonda() {
        resueltas = 0;
        estrellasRonda = 0;
        chipSel = -1;
        imgSel = -1;
        resuelta = new boolean[NOMBRES.length];
        errores = new int[NOMBRES.length];

        List<Integer> orden = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
        Collections.shuffle(orden);

        for (int i = 0; i < NOMBRES.length; i++) {
            imagenes[i].setAlpha(1f);
            imagenes[i].setScaleX(1f);
            imagenes[i].setScaleY(1f);
            imagenes[i].setEnabled(true);
            final int ii = i;
            imagenes[i].setOnClickListener(v -> tocarImagen(ii));

            chips[i].setText(palabras[orden.get(i)].palabra);
            chips[i].setTag(orden.get(i));
            chips[i].setBackgroundResource(FONDOS[i]);
            chips[i].setScaleX(1f);
            chips[i].setScaleY(1f);
            chips[i].setEnabled(true);
            final int ci = i;
            chips[i].setOnClickListener(v -> tocarChip(ci));
        }
    }

    private void tocarChip(int c) {
        if (!chips[c].isEnabled()) return;
        if (chipSel >= 0) escala(chips[chipSel], 1f);
        chipSel = c;
        escala(chips[c], 1.1f);
        evaluar();
    }

    private void tocarImagen(int i) {
        if (!imagenes[i].isEnabled()) return;
        if (imgSel >= 0) escala(imagenes[imgSel], 1f);
        imgSel = i;
        escala(imagenes[i], 1.1f);
        evaluar();
    }

    private void escala(View v, float e) {
        v.animate().scaleX(e).scaleY(e).setDuration(120).start();
    }

    private void evaluar() {
        if (chipSel < 0 || imgSel < 0) return;

        TextView chip = chips[chipSel];
        ImageView img = imagenes[imgSel];
        int palabraDelChip = (Integer) chip.getTag();

        if (palabraDelChip == imgSel) {
            resuelta[imgSel] = true;
            resueltas++;
            chip.setBackgroundResource(R.drawable.bg_field_green);
            chip.setEnabled(false);
            img.setAlpha(0.35f);
            img.setEnabled(false);
            escala(chip, 1f);
            escala(img, 1f);
            Feedback.acierto(chip);

            Palabra p = palabras[palabraDelChip];
            if (!juego().palabraDesbloqueada(p.id)) {
                juego().desbloquearPalabra(p.id);
                if (errores[palabraDelChip] == 0) estrellasRonda++;
            }
            chipSel = -1;
            imgSel = -1;
            if (resueltas == NOMBRES.length) terminarRonda();
        } else {
            errores[palabraDelChip]++;
            Feedback.error(chip);
            Feedback.error(img);
            Toast.makeText(this, "Ese no es, ¡inténtalo de nuevo!", Toast.LENGTH_SHORT).show();
            escala(chip, 1f);
            escala(img, 1f);
            chipSel = -1;
            imgSel = -1;
        }
    }

    private void terminarRonda() {
        progreso().registrarActividad(estrellasRonda);
        String msg = "¡Relacionaste todas las palabras!"
                + (estrellasRonda > 0 ? "\n\n¡Ganaste +" + estrellasRonda
                + (estrellasRonda == 1 ? " estrella!" : " estrellas!") : "")
                + "\nLas palabras nuevas están en “Mis palabras”.";
        new AlertDialog.Builder(this)
                .setTitle("¡Excelente!")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Jugar otra vez", (d, w) -> iniciarRonda())
                .setNegativeButton("Volver", (d, w) -> finish())
                .show();
    }
}