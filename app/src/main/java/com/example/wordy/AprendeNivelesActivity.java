package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityAprendeNivelesBinding;

public class AprendeNivelesActivity extends AppCompatActivity {

    private ActivityAprendeNivelesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAprendeNivelesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.btnMenu.setOnClickListener(v -> Navegacion.mostrarMenu(this));
        binding.footer.btnHome.setOnClickListener(v -> Navegacion.irAHome(this));
        binding.footer.btnRegresar.setOnClickListener(v -> finish());
        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));

        // Por ahora cualquier nivel abre el ejercicio de ejemplo (la lógica va después)
        binding.mapa.nivel1.setOnClickListener(v -> abrirPractica());
        binding.mapa.nivel2.setOnClickListener(v -> abrirPractica());
        binding.mapa.nivel3.setOnClickListener(v -> abrirPractica());
        binding.mapa.nivel4.setOnClickListener(v -> abrirPractica());
        binding.mapa.nivel5.setOnClickListener(v -> abrirPractica());
    }

    private void abrirPractica() {
        startActivity(new Intent(this, PracticaPalabrasActivity.class));
    }
}