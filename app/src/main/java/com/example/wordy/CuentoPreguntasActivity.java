package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.wordy.databinding.ActivityCuentoPreguntasBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Trivia final del cuento. Igual que la web: exige responder todo y el SERVIDOR califica y guarda
 * en PROGRESO_CUENTO con POST /api/cuentos/finalizar (el ticket evita guardar dos veces).
 */
public class CuentoPreguntasActivity extends BaseWordyActivity {

    public static final String EXTRA_ID_CUENTO = "id_cuento";
    public static final String EXTRA_TITULO = "titulo_nivel";
    public static final String EXTRA_PORTADA = "portada";
    public static final String EXTRA_PREGUNTAS = "preguntas";
    public static final String EXTRA_TICKET = "ticket";
    public static final String EXTRA_H_ACIERTOS = "h_aciertos";
    public static final String EXTRA_H_INTENTOS = "h_intentos";

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal,
            R.drawable.bg_field_purple, R.drawable.bg_field_pink};

    private ActivityCuentoPreguntasBinding binding;
    private JSONArray preguntas;
    private int[] elegidas;                           // id_opcion elegido por pregunta (-1 = sin responder)
    private final List<List<TextView>> chips = new ArrayList<>();
    private boolean enviando = false;
    private JSONObject resultado;                     // respuesta de /finalizar (null = aún no se califica)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityCuentoPreguntasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvSubtitulo.setText(getIntent().getStringExtra(EXTRA_TITULO));
        try {
            preguntas = new JSONArray(getIntent().getStringExtra(EXTRA_PREGUNTAS));
        } catch (Exception e) {
            preguntas = new JSONArray();
        }
        if (preguntas.length() == 0) {
            aviso("Este cuento todavía no tiene preguntas.");
            finish();
            return;
        }
        elegidas = new int[preguntas.length()];
        java.util.Arrays.fill(elegidas, -1);
        construirPreguntas();
        binding.footer.btnRevisar.setOnClickListener(v -> revisar());
    }

    private void construirPreguntas() {
        for (int q = 0; q < preguntas.length(); q++) {
            JSONObject p = preguntas.optJSONObject(q);
            JSONArray ops = p.optJSONArray("opciones");
            View bloque = getLayoutInflater().inflate(R.layout.item_pregunta, binding.llPreguntas, false);
            ((TextView) bloque.findViewById(R.id.tvEnunciado)).setText((q + 1) + ". " + ApiClient.txt(p, "enunciado"));

            LinearLayout cont = bloque.findViewById(R.id.llOpciones);
            boolean fila = sonCortas(ops);
            cont.setOrientation(fila ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

            List<TextView> lista = new ArrayList<>();
            for (int o = 0; ops != null && o < ops.length(); o++) {
                JSONObject op = ops.optJSONObject(o);
                TextView chip = (TextView) getLayoutInflater().inflate(R.layout.item_opcion, cont, false);
                chip.setText(((char) ('a' + o)) + ")  " + ApiClient.txt(op, "texto"));
                chip.setTag(op.optInt("id"));
                chip.setBackgroundResource(FONDOS[o % FONDOS.length]);
                LinearLayout.LayoutParams lp = fila
                        ? new LinearLayout.LayoutParams(0, dp(42), 1f)
                        : new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
                if (fila) lp.setMarginEnd(dp(8)); else lp.topMargin = dp(8);
                chip.setLayoutParams(lp);
                final int qi = q, oi = o;
                chip.setOnClickListener(v -> elegir(qi, oi));
                cont.addView(chip);
                lista.add(chip);
            }
            chips.add(lista);
            binding.llPreguntas.addView(bloque);

            // Portada del cuento después de la 1.ª pregunta (como en el diseño)
            String portada = getIntent().getStringExtra(EXTRA_PORTADA);
            if (q == 0 && portada != null && !portada.isEmpty()) {
                ImageView iv = new ImageView(this);
                iv.setAdjustViewBounds(true);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(130));
                lp.topMargin = dp(10);
                binding.llPreguntas.addView(iv, lp);
                ImagenUrl.cargar(iv, portada, 0);
            }
        }
    }

    private boolean sonCortas(JSONArray ops) {
        if (ops == null) return true;
        for (int i = 0; i < ops.length(); i++) {
            if (ApiClient.txt(ops.optJSONObject(i), "texto").length() > 10) return false;
        }
        return ops.length() <= 3;
    }

    private void elegir(int q, int o) {
        if (resultado != null || enviando) return;
        List<TextView> lista = chips.get(q);
        elegidas[q] = (Integer) lista.get(o).getTag();
        for (int i = 0; i < lista.size(); i++) lista.get(i).setAlpha(i == o ? 1f : 0.5f);
    }

    private void revisar() {
        if (resultado != null) {          // ya calificado: muestra otra vez el resultado
            mostrarResultado(resultado);
            return;
        }
        if (enviando) return;
        for (int r : elegidas) {
            if (r < 0) {
                Toast.makeText(this, "Completa la actividad antes de enviar.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        JSONObject cuerpo = new JSONObject();
        try {
            JSONObject respuestas = new JSONObject();
            for (int q = 0; q < preguntas.length(); q++) {
                respuestas.put(String.valueOf(preguntas.optJSONObject(q).optInt("id")), elegidas[q]);
            }
            cuerpo.put("idUsuario", idUsuario());
            cuerpo.put("idCuento", getIntent().getIntExtra(EXTRA_ID_CUENTO, 0));
            cuerpo.put("ticket", getIntent().getStringExtra(EXTRA_TICKET) == null ? "" : getIntent().getStringExtra(EXTRA_TICKET));
            cuerpo.put("hAciertos", getIntent().getIntExtra(EXTRA_H_ACIERTOS, 0));
            cuerpo.put("hIntentos", getIntent().getIntExtra(EXTRA_H_INTENTOS, 0));
            cuerpo.put("respuestas", respuestas);
        } catch (JSONException e) {
            return;
        }

        enviando = true;
        binding.footer.btnRevisar.setAlpha(0.5f);
        ApiClient.post("cuentos/finalizar", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                enviando = false;
                if (cerrada()) return;
                binding.footer.btnRevisar.setAlpha(1f);
                if (!json.optBoolean("ok", false)) {
                    aviso(json.optString("error", "Error en la evaluación, intenta de nuevo."));
                    return;
                }
                resultado = json;
                marcarRespuestas(json.optJSONArray("detalle"));
                ProgresoApi.cargarResumen(CuentoPreguntasActivity.this, (r, e) -> { });
                mostrarResultado(json);
            }

            @Override
            public void onError(String mensaje) {
                enviando = false;
                if (cerrada()) return;
                binding.footer.btnRevisar.setAlpha(1f);
                aviso(mensaje);
            }
        });
    }

    /** Verde = respuesta correcta, coral = la que eligió mal (el servidor manda los textos en "detalle"). */
    private void marcarRespuestas(JSONArray detalle) {
        if (detalle == null) return;
        View ultimaBuena = null, ultimaMala = null;
        for (int q = 0; q < chips.size() && q < detalle.length(); q++) {
            JSONObject d = detalle.optJSONObject(q);
            String correcta = ApiClient.txt(d, "correcta");
            for (TextView c : chips.get(q)) {
                c.setAlpha(1f);
                String texto = c.getText().toString().replaceFirst("^[a-z]\\)\\s*", "");
                boolean esElegida = ((Integer) c.getTag()) == elegidas[q];
                if (texto.equals(correcta)) {
                    c.setBackgroundResource(R.drawable.bg_field_green);
                    if (esElegida) ultimaBuena = c;
                } else if (esElegida) {
                    c.setBackgroundResource(R.drawable.bg_field_coral);
                    ultimaMala = c;
                }
            }
        }
        if (ultimaMala != null) Feedback.error(ultimaMala);
        else if (ultimaBuena != null) Feedback.acierto(ultimaBuena);
    }

    private void mostrarResultado(JSONObject res) {
        int aciertos = res.optInt("aciertos");
        int total = res.optInt("total");
        int estrellas = res.optInt("estrellas");
        String titulo = aciertos == total ? "¡Lo hiciste perfecto!"
                : (aciertos * 2 >= total ? "¡Muy bien, súper lector!" : "¡Buen intento, sigue practicando!");

        StringBuilder estrellasTxt = new StringBuilder();
        for (int s = 1; s <= 3; s++) estrellasTxt.append(s <= estrellas ? "★" : "☆");

        StringBuilder msg = new StringBuilder();
        msg.append(estrellasTxt).append("\n\nAcertaste ").append(aciertos).append(" de ").append(total)
                .append(" preguntas. ¡Completaste el cuento!");
        if (aciertos < total) msg.append("\nLas respuestas correctas están en verde.");
        String guardado = ApiClient.txt(res, "mensaje");
        if (!guardado.isEmpty()) msg.append("\n\n").append(guardado);

        final int siguiente = res.isNull("siguienteNivel") ? 0 : res.optInt("siguienteNivel", 0);
        final int nivel = res.optInt("nivel", 1);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(msg.toString())
                .setCancelable(true)
                .setNeutralButton("Ver el mapa", (d, w) -> volverANiveles());
        if (siguiente > 0) {
            b.setPositiveButton("Siguiente cuento", (d, w) -> abrirCuento(siguiente));
            b.setNegativeButton("Ver respuestas", null);
        } else {
            b.setPositiveButton("Leer otra vez", (d, w) -> abrirCuento(nivel));
            b.setNegativeButton("Ver respuestas", null);
        }
        b.show();
    }

    private void abrirCuento(int nivel) {
        Intent i = new Intent(this, CuentoLecturaActivity.class);
        i.putExtra(CuentoLecturaActivity.EXTRA_NIVEL, nivel);
        startActivity(i);
        finish();
    }

    private void volverANiveles() {
        Intent i = new Intent(this, CuentaCuentosNivelesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}
