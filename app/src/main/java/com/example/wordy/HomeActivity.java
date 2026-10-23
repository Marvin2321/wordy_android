package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityHomeBinding;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private ProgresoManager progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String usuarioId = SesionManager.usuarioId(this);
        if (usuarioId == null) {            // sin sesión: regresa al login
            irALogin();
            return;
        }

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progreso = new ProgresoManager(new ProgresoLocal(this), usuarioId);

        mostrarRefranDelDia();
        configurarBotones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progreso != null) mostrarProgreso(progreso.cargar());
    }

    private void mostrarProgreso(Usuario u) {
        binding.tvRacha.setText(String.valueOf(u.racha));
        binding.tvEstrellas.setText(String.valueOf(u.estrellas));
    }

    /** Un refrán distinto cada día (rota por día del año). */
    private void mostrarRefranDelDia() {
        String[] refranes = getResources().getStringArray(R.array.refranes);
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        String refran = refranes[dia % refranes.length];
        binding.tvRefran.setText(getString(R.string.refran_formato, refran));
    }

    private void configurarBotones() {
        binding.btnContinuar.setOnClickListener(v -> {
            // TODO: abrir la actividad/juego. Al terminar, llamar:
            //   progreso.registrarActividad(correcta);
            //   mostrarProgreso(...)
            Toast.makeText(this, "¡Próximamente!", Toast.LENGTH_SHORT).show();
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