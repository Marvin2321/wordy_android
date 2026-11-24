package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityAprendeActividadesBinding;

public class AprendeActividadesActivity extends AppCompatActivity {

    private ActivityAprendeActividadesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAprendeActividadesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.btnMenu.setOnClickListener(v -> Navegacion.mostrarMenu(this));
        binding.footer.btnHome.setOnClickListener(v -> Navegacion.irAHome(this));
        binding.footer.btnRegresar.setOnClickListener(v -> finish());

        binding.btnEjercicios.setOnClickListener(v ->
                startActivity(new Intent(this, AprendeNivelesActivity.class)));
        binding.btnRelacionar.setOnClickListener(v ->
                startActivity(new Intent(this, RelacionarImagenesActivity.class)));
    }
}