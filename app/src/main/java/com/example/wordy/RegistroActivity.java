package com.example.wordy;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordy.databinding.ActivityRegistroBinding;

import org.json.JSONException;
import org.json.JSONObject;

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
            int tipo = binding.spTipoCuenta.getSelectedItemPosition();
            String correo = binding.etCorreo.getText().toString().trim();
            String usuario = binding.etUsuario.getText().toString().trim();
            String contrasena = binding.etContrasena.getText().toString();

            String error = Validador.validarRegistro(tipo, correo, usuario, contrasena);
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }
            registrarEnApi(tipo, correo, usuario, contrasena);
        });
    }

    /** Posición del spinner → valor que entiende la API ("tutor" = rol 1, cualquier otro = rol 3). */
    private String tipoParaApi(int posicion) {
        switch (posicion) {
            case 2:  return "tutor";   // PADRE / TUTOR
            case 3:  return "docente"; // DOCENTE (hoy la API lo guarda como rol 3)
            default: return "nino";    // NIÑO / NIÑA
        }
    }

    private void registrarEnApi(int tipo, String correo, String usuario, String contrasena) {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("nombre", usuario);          // la app aún no pide nombre real
            cuerpo.put("usuario", usuario);
            cuerpo.put("contrasena", contrasena);
            cuerpo.put("tipo_cuenta", tipoParaApi(tipo));
            cuerpo.put("correo", correo);           // se guarda en USUARIO.correo
        } catch (JSONException e) {
            Toast.makeText(this, "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnRegistrar.setEnabled(false);

        ApiClient.post("registro", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (isFinishing() || isDestroyed()) return;
                if (!json.optBoolean("ok", false)) {
                    binding.btnRegistrar.setEnabled(true);
                    Toast.makeText(RegistroActivity.this, json.optString("error", "No se pudo registrar la cuenta."),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(RegistroActivity.this, "¡Cuenta creada!", Toast.LENGTH_SHORT).show();
                finish(); // regresa al login
            }

            @Override
            public void onError(String mensaje) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnRegistrar.setEnabled(true);
                Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}