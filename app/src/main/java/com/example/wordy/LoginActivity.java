package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        KeyboardScroll.configurar(this, binding.scrollLogin, binding.contenidoLogin,
                binding.etUsuario, binding.etContrasena);
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
        binding.btnIngresar.setOnClickListener(v -> {
            int tipo = binding.spTipoCuenta.getSelectedItemPosition();
            String usuario = binding.etUsuario.getText().toString().trim();
            String contrasena = binding.etContrasena.getText().toString();

            String error = Validador.validarLogin(tipo, usuario, contrasena);
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO (BD): verificar usuario y contraseña antes de continuar
            SesionManager.iniciar(this, usuario.toLowerCase(), usuario, tipo);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        binding.btnCrearCuenta.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }}