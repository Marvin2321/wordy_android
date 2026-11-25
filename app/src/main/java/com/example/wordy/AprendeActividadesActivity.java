package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import com.example.wordy.databinding.ActivityAprendeActividadesBinding;

public class AprendeActividadesActivity extends BaseWordyActivity {

    private ActivityAprendeActividadesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAprendeActividadesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnEjercicios.setOnClickListener(v ->
                startActivity(new Intent(this, AprendeNivelesActivity.class)));
        binding.btnRelacionar.setOnClickListener(v ->
                startActivity(new Intent(this, RelacionarImagenesActivity.class)));
    }
}