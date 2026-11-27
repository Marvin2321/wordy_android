package com.example.wordy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Control Parental (CU 10 y 11), leyendo WordyDB:
 *   GET  /api/nino/vinculados?idTutor=N          ¿ya tiene un niño vinculado? (PERFIL_NINO.id_tutor)
 *   POST /api/nino/vincular                      vincula con usuario + contraseña (+ correo opcional)
 *   GET  /api/cuentos/progreso/{idNino}          PROGRESO_CUENTO   (mismo endpoint pensado para la web)
 *   GET  /api/palabras/progreso/{idNino}         INTENTO, PROGRESO_MODULO, PALABRA_DESBLOQUEADA
 * Lo que el niño juegue en la web o en el celular aparece aquí, porque todo sale de la misma BD.
 */
public class ControlParentalActivity extends BaseWordyActivity {

    private LinearLayout layoutVincular;
    private LinearLayout layoutResumenVinculado;
    private EditText etUsuarioNino;
    private EditText etContrasenaNino;
    private EditText etCorreoNino;
    private Button btnVincular;
    private TextView tvNinoVinculado;

    private TextView tvEstrellasCuentos, tvIntentosCuentos, tvAciertosCuentos, tvNivelCuentos;
    private TextView tvEstrellasPalabras, tvIntentosPalabras, tvAciertosPalabras, tvNivelPalabras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;

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

        btnVincular = vista.findViewById(R.id.btnVincular);
        btnVincular.setOnClickListener(v -> intentarVincular());

        layoutVincular.setVisibility(View.GONE);
        layoutResumenVinculado.setVisibility(View.GONE);

        if (!SesionManager.esTutor(this)) {
            aviso("Control Parental es solo para cuentas de tutor.");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutVincular != null && SesionManager.esTutor(this)) cargarVinculados();
    }

    // ================================================================ VINCULACIÓN
    private void cargarVinculados() {
        ApiClient.get("nino/vinculados?idTutor=" + ApiClient.q(idUsuario()), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                JSONArray ninos = json.optJSONArray("ninos");
                if (ninos == null || ninos.length() == 0) {
                    layoutVincular.setVisibility(View.VISIBLE);
                    layoutResumenVinculado.setVisibility(View.GONE);
                    return;
                }
                mostrarNino(ninos.optJSONObject(0));   // el diseño muestra un niño (el primero vinculado)
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                layoutVincular.setVisibility(View.VISIBLE);
                aviso(mensaje);
            }
        });
    }

    private void intentarVincular() {
        String usuario = etUsuarioNino.getText().toString().trim();
        String contrasena = etContrasenaNino.getText().toString();
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
        if (!correo.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoNino.setError("Escribe un correo válido (o déjalo vacío)");
            etCorreoNino.requestFocus();
            return;
        }

        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idTutor", idUsuario());
            cuerpo.put("usuarioNino", usuario);
            cuerpo.put("contrasenaNino", contrasena);
            cuerpo.put("correoNino", correo);
        } catch (JSONException e) {
            return;
        }
        btnVincular.setEnabled(false);
        ApiClient.post("nino/vincular", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                btnVincular.setEnabled(true);
                if (!json.optBoolean("ok", false)) {
                    aviso(json.optString("error", "No se pudo vincular."));
                    return;
                }
                etUsuarioNino.setText("");
                etContrasenaNino.setText("");
                etCorreoNino.setText("");
                Toast.makeText(ControlParentalActivity.this, "Perfil vinculado", Toast.LENGTH_SHORT).show();
                mostrarNino(json.optJSONObject("nino"));
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                btnVincular.setEnabled(true);
                aviso(mensaje);   // p. ej. "No se encontró ningún niño con ese usuario"
            }
        });
    }

    // ================================================================ PROGRESO
    private void mostrarNino(JSONObject nino) {
        if (nino == null) return;
        layoutVincular.setVisibility(View.GONE);
        layoutResumenVinculado.setVisibility(View.VISIBLE);
        String nombre = ApiClient.txt(nino, "nombre");
        if (nombre.isEmpty()) nombre = ApiClient.txt(nino, "usuario");
        tvNinoVinculado.setText("Niño vinculado:\n" + nombre);

        String idNino = String.valueOf(nino.optInt("idUsuario"));
        tvEstrellasCuentos.setText("…");
        tvEstrellasPalabras.setText("…");
        cargarCuentos(idNino);
        cargarPalabras(idNino);
    }

    private void cargarCuentos(String idNino) {
        ApiClient.get("cuentos/progreso/" + ApiClient.q(idNino), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                JSONArray filas = json.optJSONArray("filas");
                int estrellas = 0, intentos = 0, aciertos = 0, nivel = 0;
                for (int k = 0; filas != null && k < filas.length(); k++) {
                    JSONObject f = filas.optJSONObject(k);
                    estrellas += f.optInt("estrellas");
                    int a = f.optInt("aciertosHuecos") + f.optInt("aciertosPreguntas");
                    aciertos += a;
                    intentos += a + f.optInt("erroresHuecos") + f.optInt("erroresPreguntas");
                    nivel = Math.max(nivel, f.optInt("nivel"));
                }
                tvEstrellasCuentos.setText(String.valueOf(estrellas));
                tvIntentosCuentos.setText("Número de Intentos:\n" + intentos);
                tvAciertosCuentos.setText("Número de aciertos:\n" + aciertos);
                tvNivelCuentos.setText(nivel == 0 ? "Nivel:\nAún no hay progreso registrado" : "Nivel:\n" + nivel);
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                tvEstrellasCuentos.setText("0");
                tvNivelCuentos.setText("Nivel:\n" + mensaje);
            }
        });
    }

    private void cargarPalabras(String idNino) {
        ApiClient.get("palabras/progreso/" + ApiClient.q(idNino), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                int aciertos = json.optInt("aciertos");
                int errores = json.optInt("errores");
                int nivel = 0;
                JSONArray niveles = json.optJSONArray("niveles");
                for (int k = 0; niveles != null && k < niveles.length(); k++) {
                    JSONObject n = niveles.optJSONObject(k);
                    if (!"bloqueado".equals(n.optString("estado"))) nivel = Math.max(nivel, n.optInt("nivel"));
                }
                JSONArray coleccion = json.optJSONArray("palabrasDesbloqueadas");
                int palabras = coleccion == null ? 0 : coleccion.length();

                tvEstrellasPalabras.setText(String.valueOf(json.optInt("estrellas")));
                tvIntentosPalabras.setText("Número de intentos:\n" + (aciertos + errores));
                tvAciertosPalabras.setText("Número de aciertos:\n" + aciertos);
                tvNivelPalabras.setText((aciertos + errores) == 0 ? "Nivel:\nAún no hay progreso registrado"
                        : "Nivel:\n" + Math.max(1, nivel) + "  ·  " + palabras + " palabras");
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                tvEstrellasPalabras.setText("0");
                tvIntentosPalabras.setText("Número de intentos:\n0");
                tvAciertosPalabras.setText("Número de aciertos:\n0");
                tvNivelPalabras.setText("Nivel:\n" + mensaje);
            }
        });
    }
}
