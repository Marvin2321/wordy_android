package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.wordy.databinding.ActivityRelacionarImagenesBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Relacionar con imágenes (CU 7). MISMA lógica que arrancarImagenes() de palabrasModulo.js:
 *   GET  /api/palabras/nivel/{n}?actividad=imagenes   imágenes revueltas + palabras revueltas (sin decir cuál va)
 *   POST /api/palabras/validar {idUsuario, idEjercicio, respuesta, modo:"juego"}  -> correcta / respuestaCorrecta
 *   POST /api/palabras/finalizar  al relacionar todas.
 * Toca una palabra y luego su imagen (o al revés).
 */
public class RelacionarImagenesActivity extends BaseWordyActivity {

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal, R.drawable.bg_field_purple,
            R.drawable.bg_field_pink, R.drawable.bg_field_coral};

    /** Una imagen del tablero (casilla). */
    private static class Casilla {
        int idEjercicio;
        View vista;
        TextView etiqueta;
        boolean resuelta = false;
        int intentos = 0;
    }

    private ActivityRelacionarImagenesBinding binding;
    private final List<Casilla> casillas = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();
    private final List<String> nuevas = new ArrayList<>();
    private int nivel = 1;
    private String ticket = "";
    private int aciertos = 0;
    private Casilla casillaSel;
    private TextView chipSel;
    private boolean enviando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityRelacionarImagenesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nivel = getIntent().getIntExtra(PracticaPalabrasActivity.EXTRA_NIVEL, 1);
        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
        binding.footer.btnRevisar.setOnClickListener(v ->
                Toast.makeText(this, "Llevas " + resueltas() + " de " + casillas.size()
                        + " imágenes relacionadas", Toast.LENGTH_SHORT).show());
        cargar();
    }

    private void cargar() {
        ApiClient.get("palabras/nivel/" + nivel + "?actividad=imagenes&idUsuario=" + ApiClient.q(idUsuario()),
                new ApiClient.Respuesta() {
                    @Override
                    public void onExito(JSONObject json) {
                        if (cerrada()) return;
                        JSONArray imagenes = json.optJSONArray("imagenes");
                        if (!json.optBoolean("ok", false) || imagenes == null || imagenes.length() == 0) {
                            mensaje("Este nivel no tiene imágenes todavía.");
                            return;
                        }
                        ticket = ApiClient.txt(json, "ticket");
                        String enun = ApiClient.txt(json, "enunciado");
                        if (!enun.isEmpty()) binding.tvInstruccion.setText(enun);
                        String avisoApi = ApiClient.txt(json, "aviso");
                        if (!avisoApi.isEmpty()) aviso(avisoApi);
                        armarTablero(imagenes, json.optJSONArray("opciones"));
                        mensaje("Toca una palabra y luego su imagen.");
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (cerrada()) return;
                        mensaje(mensaje);
                        aviso(mensaje);
                    }
                });
    }

    private void mensaje(String t) {
        binding.tvMensaje.setText(t);
    }

    // ================================================================ TABLERO
    private GridLayout.LayoutParams celda(int alto) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        lp.height = alto;
        lp.setMargins(dp(5), dp(5), dp(5), dp(5));
        return lp;
    }

    private void armarTablero(JSONArray imagenes, JSONArray opciones) {
        binding.glImagenes.removeAllViews();
        binding.glPalabras.removeAllViews();
        casillas.clear();
        chips.clear();

        for (int k = 0; k < imagenes.length(); k++) {
            JSONObject im = imagenes.optJSONObject(k);
            Casilla c = new Casilla();
            c.idEjercicio = im.optInt("idEjercicio");

            LinearLayout caja = new LinearLayout(this);
            caja.setOrientation(LinearLayout.VERTICAL);
            caja.setGravity(Gravity.CENTER_HORIZONTAL);

            FrameLayout marco = new FrameLayout(this);
            marco.setBackgroundResource(R.drawable.bg_card_white);
            TextView emoji = new TextView(this);
            emoji.setText(ApiClient.txt(im, "emoji"));
            emoji.setTextSize(44);
            emoji.setGravity(Gravity.CENTER);
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setContentDescription("Imagen " + (k + 1));
            marco.addView(emoji, new FrameLayout.LayoutParams(-1, -1));
            marco.addView(iv, new FrameLayout.LayoutParams(-1, -1));
            caja.addView(marco, new LinearLayout.LayoutParams(-1, dp(92)));
            ImagenUrl.cargar(iv, ApiClient.txt(im, "imagen"), 0);   // sin imagen en la BD: se ve el emoji

            TextView etiqueta = new TextView(this);
            etiqueta.setText("?");
            etiqueta.setGravity(Gravity.CENTER);
            etiqueta.setTextColor(ContextCompat.getColor(this, R.color.wordy_navy));
            etiqueta.setTextSize(12);
            etiqueta.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.varela_normal),
                    android.graphics.Typeface.BOLD);
            caja.addView(etiqueta, new LinearLayout.LayoutParams(-1, -2));

            c.vista = caja;
            c.etiqueta = etiqueta;
            caja.setOnClickListener(v -> tocarCasilla(c));
            binding.glImagenes.addView(caja, celda(GridLayout.LayoutParams.WRAP_CONTENT));
            casillas.add(c);
        }

        for (int k = 0; opciones != null && k < opciones.length(); k++) {
            TextView chip = (TextView) getLayoutInflater().inflate(R.layout.item_opcion, binding.glPalabras, false);
            chip.setText(opciones.optString(k));
            chip.setBackgroundResource(FONDOS[k % FONDOS.length]);
            chip.setOnClickListener(v -> tocarChip(chip));
            binding.glPalabras.addView(chip, celda(dp(40)));
            chips.add(chip);
        }
    }

    private void escala(View v, float e) {
        v.animate().scaleX(e).scaleY(e).setDuration(120).start();
    }

    private void tocarChip(TextView chip) {
        if (enviando || !chip.isEnabled()) return;
        if (chipSel != null) escala(chipSel, 1f);
        chipSel = chip;
        escala(chip, 1.1f);
        intentar();
    }

    private void tocarCasilla(Casilla c) {
        if (enviando || c.resuelta) return;
        if (casillaSel != null) escala(casillaSel.vista, 1f);
        casillaSel = c;
        escala(c.vista, 1.08f);
        intentar();
    }

    // ================================================================ VALIDAR
    private void intentar() {
        if (chipSel == null || casillaSel == null) return;
        final Casilla c = casillaSel;
        final TextView chip = chipSel;
        final String texto = chip.getText().toString();

        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idUsuario", idUsuario());
            cuerpo.put("idEjercicio", c.idEjercicio);
            cuerpo.put("respuesta", texto);
            cuerpo.put("modo", "juego");
        } catch (JSONException e) {
            return;
        }
        enviando = true;
        ApiClient.post("palabras/validar", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                enviando = false;
                if (cerrada()) return;
                escala(chip, 1f);
                escala(c.vista, 1f);
                chipSel = null;
                casillaSel = null;
                c.intentos++;
                if (json.optBoolean("correcta", false)) {
                    if (c.intentos == 1) aciertos++;
                    String palabra = ApiClient.txt(json, "palabra");
                    if (json.optBoolean("desbloqueada", false) && !palabra.isEmpty() && !nuevas.contains(palabra)) {
                        nuevas.add(palabra);
                    }
                    c.resuelta = true;
                    c.etiqueta.setText(texto + " ✓");
                    c.vista.setAlpha(0.55f);
                    chip.setBackgroundResource(R.drawable.bg_field_green);
                    chip.setEnabled(false);
                    Feedback.acierto(chip);
                    if (resueltas() == casillas.size()) {
                        mensaje("¡Excelente! Relacionaste todas las imágenes. 🎉");
                        binding.glImagenes.postDelayed(() -> { if (!cerrada()) finalizar(); }, 1200);
                    } else {
                        mensaje("¡Muy bien! Sigue con las demás imágenes.");
                    }
                } else {
                    // CU 7, flujo alterno: se indica la respuesta correcta y se permite reintentar
                    String correcta = ApiClient.txt(json, "respuestaCorrecta");
                    c.etiqueta.setText(correcta.isEmpty() ? "¡Casi!" : "¡Casi! Es: " + correcta);
                    Feedback.error(chip);
                    Feedback.error(c.vista);
                    mensaje("Esa no coincide: mira la pista e inténtalo otra vez.");
                }
            }

            @Override
            public void onError(String mensaje) {
                enviando = false;
                if (cerrada()) return;
                aviso(mensaje + " Intenta de nuevo.");
            }
        });
    }

    private int resueltas() {
        int n = 0;
        for (Casilla c : casillas) if (c.resuelta) n++;
        return n;
    }

    private void finalizar() {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idUsuario", idUsuario());
            cuerpo.put("ticket", ticket);
            cuerpo.put("nivel", nivel);
            cuerpo.put("actividad", "imagenes");
            cuerpo.put("aciertos", aciertos);
            cuerpo.put("total", casillas.size());
        } catch (JSONException e) {
            return;
        }
        ApiClient.post("palabras/finalizar", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                if (!json.optBoolean("ok", false)) {
                    reintentar(json.optString("error", "No pudimos guardar tu resultado"));
                    return;
                }
                ProgresoApi.cargarResumen(RelacionarImagenesActivity.this, (r, e) -> { });
                ResultadoPalabras.mostrar(RelacionarImagenesActivity.this, json, nuevas, RelacionarImagenesActivity.class);
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                reintentar(mensaje);
            }
        });
    }

    private void reintentar(String texto) {
        new AlertDialog.Builder(this)
                .setTitle("Ups")
                .setMessage(texto + ". Toca \"Reintentar\".")
                .setCancelable(false)
                .setPositiveButton("Reintentar", (d, w) -> finalizar())
                .setNegativeButton("Salir", (d, w) -> finish())
                .show();
    }
}
