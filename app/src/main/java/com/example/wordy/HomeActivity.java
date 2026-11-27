package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;

import com.example.wordy.databinding.ActivityHomeBinding;

import java.util.Calendar;

/** Pantalla de inicio (inicio.html): racha, estrellas y refrán del día. Racha y estrellas vienen de WordyDB. */
public class HomeActivity extends BaseWordyActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (sinSesion()) return;   // la base ya manda al login

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());   // aquí se envuelve en el NavigationDrawer

        mostrarRefranDelDia();
        configurarBotones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) return;
        // Primero lo último conocido (instantáneo) y luego lo que diga la BD
        mostrarProgreso(SesionManager.racha(this), SesionManager.estrellas(this));
        ProgresoApi.cargarResumen(this, (racha, estrellas) -> {
            if (!isFinishing() && !isDestroyed()) mostrarProgreso(racha, estrellas);
        });
    }

    private void mostrarProgreso(int racha, int estrellas) {
        binding.tvRacha.setText(String.valueOf(racha));
        binding.tvEstrellas.setText(String.valueOf(estrellas));
    }

    /** Un refrán distinto cada día (la web también los tiene en una lista: tabla REFRAN_FRASE aún sin usar). */
    private void mostrarRefranDelDia() {
        String[] refranes = getResources().getStringArray(R.array.refranes);
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        String refran = refranes[dia % refranes.length];
        binding.tvRefran.setText(getString(R.string.refran_formato, refran));
    }

    private void configurarBotones() {
        binding.btnContinuar.setOnClickListener(v -> {
            binding.btnContinuar.setEnabled(false);
            Navegacion.continuarAventura(this, () -> {
                if (binding != null) binding.btnContinuar.setEnabled(true);
            });
        });

        binding.btnCerrarSesion.setOnClickListener(v -> {
            SesionManager.cerrar(this);
            irALogin();
        });
    }

    private void irALogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}