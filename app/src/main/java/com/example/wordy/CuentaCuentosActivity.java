package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import com.example.wordy.databinding.ActivityCuentaCuentosBinding;

public class CuentaCuentosActivity extends BaseWordyActivity {

    private ActivityCuentaCuentosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityCuentaCuentosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnJugar.setOnClickListener(v -> {
            ProgresoApi.registrarDia(this);          // igual que el botón "¡Jugar ahora!" de la web
            startActivity(new Intent(this, CuentaCuentosNivelesActivity.class));
        });
    }
}
