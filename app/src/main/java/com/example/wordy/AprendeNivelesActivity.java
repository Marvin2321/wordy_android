package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wordy.databinding.ActivityAprendeNivelesBinding;

public class AprendeNivelesActivity extends BaseWordyActivity {

    private ActivityAprendeNivelesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAprendeNivelesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        configurarMapa();
    }

    private void configurarMapa() {
        View[] nodos = {binding.mapa.nivel1, binding.mapa.nivel2, binding.mapa.nivel3,
                binding.mapa.nivel4, binding.mapa.nivel5};
        for (int i = 0; i < nodos.length; i++) {
            final int nivel = i + 1;
            final boolean libre = juego().nivelPalabrasDesbloqueado(nivel);
            nodos[i].setAlpha(libre ? 1f : 0.4f);
            nodos[i].setOnClickListener(v -> {
                if (!libre) {
                    Toast.makeText(this, "¡Completa el nivel anterior para desbloquear este!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i2 = new Intent(this, PracticaPalabrasActivity.class);
                i2.putExtra(PracticaPalabrasActivity.EXTRA_NIVEL, nivel);
                startActivity(i2);
            });
        }
    }
}