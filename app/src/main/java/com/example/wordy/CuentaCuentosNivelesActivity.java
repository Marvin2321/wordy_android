package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityCuentaCuentosNivelesBinding;

public class CuentaCuentosNivelesActivity extends AppCompatActivity {

    private ActivityCuentaCuentosNivelesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCuentaCuentosNivelesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.btnMenu.setOnClickListener(v -> Navegacion.mostrarMenu(this));
        binding.footer.btnHome.setOnClickListener(v -> Navegacion.irAHome(this));
        binding.footer.btnRegresar.setOnClickListener(v -> finish());

        // Por ahora cualquier nivel abre el cuento de ejemplo (la lógica va después)
        binding.mapa.nivel1.setOnClickListener(v -> abrirCuento());
        binding.mapa.nivel2.setOnClickListener(v -> abrirCuento());
        binding.mapa.nivel3.setOnClickListener(v -> abrirCuento());
        binding.mapa.nivel4.setOnClickListener(v -> abrirCuento());
        binding.mapa.nivel5.setOnClickListener(v -> abrirCuento());
    }

    private void abrirCuento() {
        startActivity(new Intent(this, CuentoLecturaActivity.class));
    }
}