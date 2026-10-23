package com.example.wordy;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityRegistroBinding;

public class RegistroActivity extends AppCompatActivity {

    private ActivityRegistroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        KeyboardScroll.configurar(this, binding.scrollRegistro, binding.contenidoRegistro,
                binding.etCorreo, binding.etUsuario, binding.etContrasena);
        configurarSpinner();
        configurarBotones();
    }

    private void configurarSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_cuenta, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        binding.spTipoCuenta.setAdapter(adapter);
    }

    private void configurarBotones() {
        binding.btnRegistrar.setOnClickListener(v -> {
            String error = Validador.validarRegistro(
                    binding.spTipoCuenta.getSelectedItemPosition(),
                    binding.etCorreo.getText().toString(),
                    binding.etUsuario.getText().toString(),
                    binding.etContrasena.getText().toString());
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                // TODO: guardar la cuenta (base de datos / API)
                Toast.makeText(this, "¡Cuenta creada!", Toast.LENGTH_SHORT).show();
                finish(); // regresa al login
            }
        });
    }
}