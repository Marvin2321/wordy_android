package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordy.databinding.ActivityCuentaCuentosNivelesBinding;

import org.json.JSONObject;

/** Mapa de Cuenta cuentos: GET /api/cuentos/mapa (estado leído de PROGRESO_CUENTO). */
public class CuentaCuentosNivelesActivity extends BaseWordyActivity {

    private ActivityCuentaCuentosNivelesBinding binding;
    private TextView[] nodos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityCuentaCuentosNivelesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        nodos = MapaNiveles.nodos(binding.mapa);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) cargarMapa();   // se refresca al volver de un cuento (o de jugar en la web)
    }

    private void cargarMapa() {
        MapaNiveles.cargando(nodos);
        ApiClient.get("cuentos/mapa?idUsuario=" + ApiClient.q(idUsuario()), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                if (!json.optBoolean("ok", false) || !json.optBoolean("hayContenido", false)) {
                    aviso("No hay contenido disponible por ahora.");
                    return;
                }
                if (!json.optBoolean("perfilNino", true)) {
                    aviso("Tu cuenta aún no tiene perfil de niño: podrás leer el nivel 1, pero no se guardará tu avance.");
                }
                MapaNiveles.pintar(nodos, json.optJSONArray("niveles"), "estado", (nivel, estado) -> {
                    if ("bloqueado".equals(estado)) {
                        Toast.makeText(CuentaCuentosNivelesActivity.this,
                                "¡Completa el cuento anterior para desbloquear este nivel!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent i = new Intent(CuentaCuentosNivelesActivity.this, CuentoLecturaActivity.class);
                    i.putExtra(CuentoLecturaActivity.EXTRA_NIVEL, nivel);
                    startActivity(i);
                });
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                aviso(mensaje);
            }
        });
    }
}
