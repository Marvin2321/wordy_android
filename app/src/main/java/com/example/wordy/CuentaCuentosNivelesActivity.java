package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wordy.databinding.ActivityCuentaCuentosNivelesBinding;

public class CuentaCuentosNivelesActivity extends BaseWordyActivity {

    private ActivityCuentaCuentosNivelesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCuentaCuentosNivelesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        configurarMapa();   // se refresca al volver de un cuento
    }

    private void configurarMapa() {
        View[] nodos = {binding.mapa.nivel1, binding.mapa.nivel2, binding.mapa.nivel3,
                binding.mapa.nivel4, binding.mapa.nivel5};
        for (int i = 0; i < nodos.length; i++) {
            final int nivel = i + 1;
            final boolean libre = juego().cuentoDesbloqueado(nivel);
            nodos[i].setAlpha(libre ? 1f : 0.4f);
            nodos[i].setOnClickListener(v -> {
                if (!libre) {
                    Toast.makeText(this, "¡Termina el cuento anterior para desbloquear este!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i2 = new Intent(this, CuentoLecturaActivity.class);
                i2.putExtra(CuentoLecturaActivity.EXTRA_NIVEL, nivel);
                startActivity(i2);
            });
        }
    }
}