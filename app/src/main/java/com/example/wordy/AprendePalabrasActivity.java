package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityAprendePalabrasBinding;

public class AprendePalabrasActivity extends AppCompatActivity {

    private ActivityAprendePalabrasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAprendePalabrasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.btnMenu.setOnClickListener(v -> Navegacion.mostrarMenu(this));
        binding.footer.btnHome.setOnClickListener(v -> Navegacion.irAHome(this));
        binding.btnJugar.setOnClickListener(v ->
                startActivity(new Intent(this, AprendeActividadesActivity.class)));
        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
    }
}