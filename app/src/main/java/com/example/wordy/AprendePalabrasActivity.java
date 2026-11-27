package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import com.example.wordy.databinding.ActivityAprendePalabrasBinding;

public class AprendePalabrasActivity extends BaseWordyActivity {

    private ActivityAprendePalabrasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityAprendePalabrasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnJugar.setOnClickListener(v -> {
            ProgresoApi.registrarDia(this);          // igual que el botón "¡Jugar ahora!" de la web
            startActivity(new Intent(this, AprendeActividadesActivity.class));
        });
        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
    }
}
