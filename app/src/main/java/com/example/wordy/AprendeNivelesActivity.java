package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordy.databinding.ActivityAprendeNivelesBinding;

import org.json.JSONObject;

/**
 * Mapa de Aprende palabras: GET /api/palabras/mapa. Cada nivel trae el estado por actividad
 * ("practica" e "imagenes"): bloqueado / disponible / completado / sin_contenido (igual que la web).
 */
public class AprendeNivelesActivity extends BaseWordyActivity {

    public static final String EXTRA_ACTIVIDAD = "actividad";
    public static final String PRACTICA = "practica";
    public static final String IMAGENES = "imagenes";

    private ActivityAprendeNivelesBinding binding;
    private TextView[] nodos;
    private String actividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityAprendeNivelesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actividad = IMAGENES.equals(getIntent().getStringExtra(EXTRA_ACTIVIDAD)) ? IMAGENES : PRACTICA;
        nodos = MapaNiveles.nodos(binding.mapa);
        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) cargarMapa();
    }

    private void cargarMapa() {
        MapaNiveles.cargando(nodos);
        ApiClient.get("palabras/mapa?idUsuario=" + ApiClient.q(idUsuario()), new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                if (!json.optBoolean("ok", false) || !json.optBoolean("hayContenido", false)) {
                    aviso("No hay contenido disponible por ahora.");
                    return;
                }
                if (!json.optBoolean("perfilNino", true)) {
                    aviso("Tu cuenta aún no tiene perfil de niño: puedes jugar, pero no se guardará tu avance.");
                }
                MapaNiveles.pintar(nodos, json.optJSONArray("niveles"), actividad, (nivel, estado) -> abrir(nivel, estado));
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                aviso(mensaje);
            }
        });
    }

    private void abrir(int nivel, String estado) {
        if ("sin_contenido".equals(estado)) {
            Toast.makeText(this, "Todavía no hay actividades de este tipo en el nivel " + nivel + ".",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if ("bloqueado".equals(estado)) {
            Toast.makeText(this, "¡Completa el nivel anterior para desbloquear este!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, IMAGENES.equals(actividad)
                ? RelacionarImagenesActivity.class : PracticaPalabrasActivity.class);
        i.putExtra(PracticaPalabrasActivity.EXTRA_NIVEL, nivel);
        startActivity(i);
    }
}
