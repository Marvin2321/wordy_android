package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.wordy.databinding.ActivityDiccionarioBinding;

public class DiccionarioActivity extends BaseWordyActivity {

    private ActivityDiccionarioBinding binding;
    private DiccionarioAdapter adapter;
    private TextView[] circulos;
    private int nivelSel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiccionarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        circulos = new TextView[]{binding.nivelD1, binding.nivelD2, binding.nivelD3,
                binding.nivelD4, binding.nivelD5};
        for (int i = 0; i < circulos.length; i++) {
            final int nivel = i + 1;
            circulos[i].setOnClickListener(v -> mostrarNivel(nivel));
        }

        adapter = new DiccionarioAdapter(this::repasar);
        GridLayoutManager lm = new GridLayoutManager(this, 2);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.esVacio(position) ? 2 : 1;
            }
        });
        binding.rvPalabras.setLayoutManager(lm);
        binding.rvPalabras.setAdapter(adapter);

        mostrarNivel(nivelInicial());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nivelSel > 0) mostrarNivel(nivelSel);   // por si se desbloquearon palabras
    }

    /** El nivel más alto que ya tiene palabras desbloqueadas (o el 1). */
    private int nivelInicial() {
        for (int n = JuegoManager.NIVELES; n >= 1; n--) {
            if (!juego().palabrasDesbloqueadas(n).isEmpty()) return n;
        }
        return 1;
    }

    private void mostrarNivel(int nivel) {
        nivelSel = nivel;
        binding.tvSubtitulo.setText(getString(R.string.subtitulo_diccionario_fmt, nivel));
        for (int i = 0; i < circulos.length; i++) {
            circulos[i].setAlpha(i + 1 == nivel ? 1f : 0.55f);
        }
        adapter.setPalabras(juego().palabrasDesbloqueadas(nivel));
    }

    /** "Repasar": abre la actividad de 4 opciones solo con esa palabra. */
    private void repasar(String idPalabra) {
        Intent i = new Intent(this, PracticaPalabrasActivity.class);
        i.putExtra(PracticaPalabrasActivity.EXTRA_REPASO_ID, idPalabra);
        startActivity(i);
    }
}