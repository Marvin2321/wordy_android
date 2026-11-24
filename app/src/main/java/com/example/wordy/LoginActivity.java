package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

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

            iniciarSesionEnApi(tipo, usuario, contrasena);
        });

        binding.btnCrearCuenta.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void iniciarSesionEnApi(int tipo, String usuario, String contrasena) {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("usuario", usuario);
            cuerpo.put("contrasena", contrasena);
        } catch (JSONException e) {
            Toast.makeText(this, "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnIngresar.setEnabled(false); // evita doble toque mientras espera

        ApiClient.post("login", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (isFinishing() || isDestroyed()) return;

                // La API responde: ok, usuario (nombre), id_usuario, id_rol
                String id = String.valueOf(json.optLong("id_usuario"));
                String nombre = json.optString("usuario", usuario);

                SesionManager.iniciar(LoginActivity.this, id, nombre, tipo);
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onError(String mensaje) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnIngresar.setEnabled(true);
                Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}