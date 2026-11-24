package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityCuentoLecturaBinding;

public class CuentoLecturaActivity extends AppCompatActivity {

    private ActivityCuentoLecturaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCuentoLecturaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.btnMenu.setOnClickListener(v -> Navegacion.mostrarMenu(this));
        binding.footer.btnHome.setOnClickListener(v -> Navegacion.irAHome(this));
        binding.footer.btnRegresar.setOnClickListener(v -> finish());
        binding.footer.btnRevisar.setOnClickListener(v ->
                startActivity(new Intent(this, CuentoPreguntasActivity.class)));

        // TODO (lógica): btnPlay reproduce el audio; cardOpciones se muestra solo cuando el audio se detiene
    }
}