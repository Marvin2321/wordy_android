package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import com.example.wordy.databinding.ActivityAprendeActividadesBinding;

/** "¡Selecciona la actividad que explorarás!": cada actividad tiene su propio mapa de 5 niveles. */
public class AprendeActividadesActivity extends BaseWordyActivity {

    private ActivityAprendeActividadesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityAprendeActividadesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnEjercicios.setOnClickListener(v -> abrirMapa(AprendeNivelesActivity.PRACTICA));
        binding.btnRelacionar.setOnClickListener(v -> abrirMapa(AprendeNivelesActivity.IMAGENES));
    }

    private void abrirMapa(String actividad) {
        Intent i = new Intent(this, AprendeNivelesActivity.class);
        i.putExtra(AprendeNivelesActivity.EXTRA_ACTIVIDAD, actividad);
        startActivity(i);
    }
}
