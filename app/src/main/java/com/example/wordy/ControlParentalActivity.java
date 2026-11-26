package com.example.wordy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ControlParentalActivity extends BaseWordyActivity {

    private static final String PREFS_PARENTAL = "wordy_control_parental";
    private static final String KEY_NINO_NOMBRE = "nino_nombre_";

    private LinearLayout layoutVincular;
    private LinearLayout layoutResumenVinculado;
    private EditText etUsuarioNino;
    private EditText etContrasenaNino;
    private EditText etCorreoNino;
    private TextView tvNinoVinculado;

    private TextView tvEstrellasCuentos;
    private TextView tvIntentosCuentos;
    private TextView tvAciertosCuentos;
    private TextView tvNivelCuentos;

    private TextView tvEstrellasPalabras;
    private TextView tvIntentosPalabras;
    private TextView tvAciertosPalabras;
    private TextView tvNivelPalabras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View vista = getLayoutInflater().inflate(R.layout.activity_control_parental, null);
        setContentView(vista);

        layoutVincular = vista.findViewById(R.id.layoutVincular);
        layoutResumenVinculado = vista.findViewById(R.id.layoutResumenVinculado);
        etUsuarioNino = vista.findViewById(R.id.etUsuarioNino);
        etContrasenaNino = vista.findViewById(R.id.etContrasenaNino);
        etCorreoNino = vista.findViewById(R.id.etCorreoNino);
        tvNinoVinculado = vista.findViewById(R.id.tvNinoVinculado);

        tvEstrellasCuentos = vista.findViewById(R.id.tvEstrellasCuentos);
        tvIntentosCuentos = vista.findViewById(R.id.tvIntentosCuentos);
        tvAciertosCuentos = vista.findViewById(R.id.tvAciertosCuentos);
        tvNivelCuentos = vista.findViewById(R.id.tvNivelCuentos);

        tvEstrellasPalabras = vista.findViewById(R.id.tvEstrellasPalabras);
        tvIntentosPalabras = vista.findViewById(R.id.tvIntentosPalabras);
        tvAciertosPalabras = vista.findViewById(R.id.tvAciertosPalabras);
        tvNivelPalabras = vista.findViewById(R.id.tvNivelPalabras);

        Button btnVincular = vista.findViewById(R.id.btnVincular);
        btnVincular.setOnClickListener(v -> intentarVincular());

        // Permite desvincular con pulsación larga en el badge del niño vinculado
        tvNinoVinculado.setOnLongClickListener(v -> {
            desvincularNino();
            return true;
        });

        actualizarEstadoPantalla();
    }

    private void intentarVincular() {
        String usuario = etUsuarioNino.getText().toString().trim();
        String contrasena = etContrasenaNino.getText().toString().trim();
        String correo = etCorreoNino.getText().toString().trim();

        if (usuario.isEmpty()) {
            etUsuarioNino.setError("Ingresa el usuario del niño");
            etUsuarioNino.requestFocus();
            return;
        }
        if (contrasena.isEmpty()) {
            etContrasenaNino.setError("Ingresa la contraseña del niño");
            etContrasenaNino.requestFocus();
            return;
        }
        if (correo.isEmpty()) {
            etCorreoNino.setError("Ingresa el correo vinculado al niño");
            etCorreoNino.requestFocus();
            return;
        }

        prefsParental().edit().putString(claveTutor(), usuario).apply();
        etUsuarioNino.setText("");
        etContrasenaNino.setText("");
        etCorreoNino.setText("");
        Toast.makeText(this, "Perfil vinculado", Toast.LENGTH_SHORT).show();
        actualizarEstadoPantalla();
    }

    private void desvincularNino() {
        prefsParental().edit().remove(claveTutor()).apply();
        Toast.makeText(this, "Perfil desvinculado", Toast.LENGTH_SHORT).show();
        actualizarEstadoPantalla();
    }

    private void actualizarEstadoPantalla() {
        String nino = prefsParental().getString(claveTutor(), null);
        boolean vinculado = nino != null && !nino.isEmpty();

        layoutVincular.setVisibility(vinculado ? View.GONE : View.VISIBLE);
        layoutResumenVinculado.setVisibility(vinculado ? View.VISIBLE : View.GONE);

        if (vinculado) {
            tvNinoVinculado.setText("Niño vinculado:\n" + nino + "\n(Mantén presionado para desvincular)");
            cargarMetricas();
        }
    }

    private void cargarMetricas() {
        Usuario u = progreso().cargar();
        int estrellasBase = (u != null) ? u.estrellas : 0;

        // Valores base listos para alimentar con tus managers/repositorios reales
        tvEstrellasCuentos.setText(String.valueOf(estrellasBase));
        tvIntentosCuentos.setText("Número de Intentos:\n0");
        tvAciertosCuentos.setText("Número de aciertos:\n0");
        tvNivelCuentos.setText("Nivel:\n1");

        tvEstrellasPalabras.setText(String.valueOf(estrellasBase));
        tvIntentosPalabras.setText("Número de intentos:\n0");
        tvAciertosPalabras.setText("Número de aciertos:\n0");
        tvNivelPalabras.setText("Nivel:\n1");
    }

    private String claveTutor() {
        String id = SesionManager.usuarioId(this);
        return KEY_NINO_NOMBRE + (id != null ? id : "default");
    }

    private SharedPreferences prefsParental() {
        return getApplicationContext().getSharedPreferences(PREFS_PARENTAL, Context.MODE_PRIVATE);
    }
}
