package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.wordy.databinding.ActivityPracticaPalabrasBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Ejercicios prácticos (CU 8 y "significado"). MISMA lógica que arrancarPractica() de palabrasModulo.js:
 *   GET  /api/palabras/nivel/{n}?actividad=practica&idUsuario=   tarjetas (sílabas + significado) SIN respuestas
 *   POST /api/palabras/validar  {idUsuario, idEjercicio, idOpcion, modo:"juego"}  guarda INTENTO y desbloquea
 *   POST /api/palabras/finalizar {idUsuario, ticket, nivel, actividad, aciertos, total}  estrellas del nivel
 *
 * Modo REPASO (desde el Diccionario): GET /api/palabras/repaso/{idPalabra} + validar con modo:"repaso".
 */
public class PracticaPalabrasActivity extends BaseWordyActivity {

    public static final String EXTRA_NIVEL = "nivel";
    public static final String EXTRA_REPASO_ID = "repaso_id";

    private static final int[] FONDOS = {
            R.drawable.bg_field_orange, R.drawable.bg_field_teal, R.drawable.bg_field_purple};

    /** Una parte de la tarjeta (sílabas o significado), igual que actual.sil / actual.sig en la web. */
    private static class Parte {
        int idEjercicio;
        int elegida = -1;          // id_opcion
        boolean resuelta = false;
        int intentos = 0;
        final List<TextView> botones = new ArrayList<>();
    }

    private ActivityPracticaPalabrasBinding binding;
    private LinearLayout filaSilabas;

    private JSONArray palabras;
    private int nivel = 1;
    private int indice = 0;
    private String ticket = "";
    private int aciertos = 0;          // respuestas correctas al primer intento
    private int total = 0;
    private final List<String> nuevas = new ArrayList<>();

    private boolean repaso = false;
    private JSONObject tarjeta;        // palabra en pantalla
    private Parte sil, sig;
    private boolean enviando = false;
    private int fallos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sinSesion()) return;
        binding = ActivityPracticaPalabrasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        filaSilabas = (LinearLayout) binding.opcionA.getParent();

        binding.btnDiccionario.getRoot().setOnClickListener(v ->
                startActivity(new Intent(this, DiccionarioActivity.class)));
        binding.ivGusanoCabeza.setOnClickListener(v -> darPista());
        binding.footer.btnRevisar.setOnClickListener(v -> revisar());
        binding.cardSilabas.setVisibility(View.GONE);
        binding.tvSignificado.setText("Cargando…");
        binding.tvPista.setText(R.string.pista_inicial);
        binding.ivPistaImg.setVisibility(View.GONE);

        int idRepaso = getIntent().getIntExtra(EXTRA_REPASO_ID, 0);
        if (idRepaso > 0) {
            repaso = true;
            cargarRepaso(idRepaso);
        } else {
            nivel = getIntent().getIntExtra(EXTRA_NIVEL, 1);
            cargarNivel();
        }
    }

    // ================================================================ CARGA
    private void cargarNivel() {
        ApiClient.get("palabras/nivel/" + nivel + "?actividad=practica&idUsuario=" + ApiClient.q(idUsuario()),
                new ApiClient.Respuesta() {
                    @Override
                    public void onExito(JSONObject json) {
                        if (cerrada()) return;
                        palabras = json.optJSONArray("palabras");
                        if (!json.optBoolean("ok", false) || palabras == null || palabras.length() == 0) {
                            fallar("Este nivel no tiene palabras todavía.");
                            return;
                        }
                        ticket = ApiClient.txt(json, "ticket");
                        String avisoApi = ApiClient.txt(json, "aviso");
                        if (!avisoApi.isEmpty()) aviso(avisoApi);
                        for (int k = 0; k < palabras.length(); k++) {
                            JSONObject t = palabras.optJSONObject(k);
                            if (t.optJSONObject("silabas") != null) total++;
                            if (t.optJSONObject("significadoPregunta") != null) total++;
                        }
                        mostrar(0);
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (cerrada()) return;
                        fallar(mensaje);
                    }
                });
    }

    private void cargarRepaso(int idPalabra) {
        ApiClient.get("palabras/repaso/" + idPalabra + "?idUsuario=" + ApiClient.q(idUsuario()),
                new ApiClient.Respuesta() {
                    @Override
                    public void onExito(JSONObject json) {
                        if (cerrada()) return;
                        if (!json.optBoolean("ok", false)) {
                            fallar(json.optString("error", "No pudimos cargar el repaso."));
                            return;
                        }
                        // Se arma una "tarjeta" con la misma forma que las del juego (solo significado)
                        try {
                            JSONObject sp = new JSONObject();
                            sp.put("idEjercicio", json.optInt("idEjercicio"));
                            sp.put("enunciado", ApiClient.txt(json, "enunciado"));
                            sp.put("palabra", ApiClient.txt(json, "palabra"));
                            sp.put("opciones", json.optJSONArray("opciones"));
                            JSONObject t = new JSONObject();
                            t.put("significadoPregunta", sp);
                            t.put("pista", ApiClient.txt(json, "pista"));
                            palabras = new JSONArray().put(t);
                        } catch (JSONException e) {
                            fallar("No pudimos cargar el repaso.");
                            return;
                        }
                        mostrar(0);
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (cerrada()) return;
                        fallar(mensaje);
                    }
                });
    }

    private void fallar(String texto) {
        binding.tvSignificado.setText(texto);
        aviso(texto);
    }

    // ================================================================ PANTALLA
    private void mostrar(int n) {
        indice = n;
        tarjeta = palabras.optJSONObject(n);
        fallos = 0;
        enviando = false;
        JSONObject s = tarjeta.optJSONObject("silabas");
        JSONObject p = tarjeta.optJSONObject("significadoPregunta");
        sil = s == null ? null : nuevaParte(s);
        sig = p == null ? null : nuevaParte(p);

        // ---- Tarjeta amarilla: sílabas ----
        binding.cardSilabas.setVisibility(sil == null ? View.GONE : View.VISIBLE);
        if (sil != null) {
            binding.tvPreguntaSilabas.setText(ApiClient.txt(s, "enunciado") + "\n" + ApiClient.txt(s, "patron"));
            filaSilabas.removeAllViews();
            JSONArray ops = s.optJSONArray("opciones");
            for (int k = 0; ops != null && k < ops.length(); k++) {
                JSONObject o = ops.optJSONObject(k);
                TextView b = (TextView) getLayoutInflater().inflate(R.layout.item_opcion, filaSilabas, false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1f);
                if (k > 0) lp.setMarginStart(dp(8));
                b.setLayoutParams(lp);
                b.setText(((char) ('a' + k)) + ")  " + ApiClient.txt(o, "texto"));
                b.setTag(o.optInt("id"));
                b.setBackgroundResource(FONDOS[k % FONDOS.length]);
                b.setOnClickListener(v -> elegir(sil, b));
                filaSilabas.addView(b);
                sil.botones.add(b);
            }
        }

        // ---- Significado (A, B, C, D) ----
        binding.llSignificados.removeAllViews();
        if (sig != null) {
            String enun = ApiClient.txt(p, "enunciado");
            if (enun.isEmpty()) enun = "¿Cuál es el significado correcto de la palabra?";
            binding.tvSignificado.setText(enun.replaceAll("\\?\\s*$", "") + " «" + ApiClient.txt(p, "palabra") + "»?");
            JSONArray ops = p.optJSONArray("opciones");
            for (int k = 0; ops != null && k < ops.length(); k++) {
                JSONObject o = ops.optJSONObject(k);
                TextView t = (TextView) getLayoutInflater().inflate(R.layout.item_significado, binding.llSignificados, false);
                t.setText(((char) ('A' + k)) + ") " + ApiClient.txt(o, "texto"));
                t.setTag(o.optInt("id"));
                t.setOnClickListener(v -> elegir(sig, t));
                binding.llSignificados.addView(t);
                sig.botones.add(t);
            }
        } else {
            binding.tvSignificado.setText("");
        }

        // ---- Pista ----
        binding.tvPista.setText(R.string.pista_inicial);
        ImagenUrl.cargar(binding.ivPistaImg, ApiClient.txt(tarjeta, "imagen"), 0);
        binding.footer.btnRevisar.setAlpha(1f);
        if (!repaso) Toast.makeText(this, "Palabra " + (n + 1) + " de " + palabras.length(), Toast.LENGTH_SHORT).show();
    }

    private Parte nuevaParte(JSONObject ej) {
        Parte p = new Parte();
        p.idEjercicio = ej.optInt("idEjercicio");
        return p;
    }

    private boolean esSilaba(Parte p) {
        return p == sil;
    }

    private void elegir(Parte p, TextView b) {
        if (p == null || p.resuelta || enviando) return;
        p.elegida = (Integer) b.getTag();
        for (TextView x : p.botones) {
            if (esSilaba(p)) {
                x.setAlpha(x == b ? 1f : 0.5f);
            } else {
                x.setBackgroundResource(x == b ? R.drawable.bg_opcion_sel : 0);
            }
        }
    }

    private void darPista() {
        String pista = tarjeta == null ? "" : ApiClient.txt(tarjeta, "pista");
        binding.tvPista.setText(getString(R.string.pista_ejemplo, pista.isEmpty() ? "lee con calma el significado." : pista));
    }

    // ================================================================ REVISAR
    private void revisar() {
        if (tarjeta == null || enviando) return;
        List<Parte> pendientes = new ArrayList<>();
        if (sil != null && !sil.resuelta) pendientes.add(sil);
        if (sig != null && !sig.resuelta) pendientes.add(sig);
        if (pendientes.isEmpty()) return;
        for (Parte p : pendientes) {
            if (p.elegida < 0) {
                Toast.makeText(this, pendientes.size() > 1 ? "Elige una opción en las dos actividades antes de revisar."
                        : "Elige una opción antes de revisar.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        enviando = true;
        binding.footer.btnRevisar.setAlpha(0.5f);
        validarEnCadena(pendientes, 0, true);
    }

    /** Valida las partes una tras otra (sin librerías de promesas) y al final decide. */
    private void validarEnCadena(List<Parte> partes, int k, boolean todoBien) {
        if (k >= partes.size()) {
            enviando = false;
            if (cerrada()) return;
            binding.footer.btnRevisar.setAlpha(1f);
            if (todoBien && (sil == null || sil.resuelta) && (sig == null || sig.resuelta)) {
                palabraTerminada();
            } else {
                fallos++;
                Feedback.error(binding.footer.btnRevisar);
                Toast.makeText(this, "¡Casi! Revisa lo que quedó en rojo e inténtalo otra vez. ¡Tú puedes!",
                        Toast.LENGTH_LONG).show();
                if (fallos >= 2) darPista();
            }
            return;
        }
        Parte p = partes.get(k);
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idUsuario", idUsuario());
            cuerpo.put("idEjercicio", p.idEjercicio);
            cuerpo.put("idOpcion", p.elegida);
            cuerpo.put("modo", repaso ? "repaso" : "juego");
        } catch (JSONException e) {
            enviando = false;
            return;
        }
        ApiClient.post("palabras/validar", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                boolean correcta = json.optBoolean("correcta", false);
                p.intentos++;
                if (correcta && p.intentos == 1 && !repaso) aciertos++;
                String palabra = ApiClient.txt(json, "palabra");
                if (json.optBoolean("desbloqueada", false) && !palabra.isEmpty() && !nuevas.contains(palabra)) {
                    nuevas.add(palabra);
                }
                marcar(p, correcta);
                validarEnCadena(partes, k + 1, todoBien && correcta);
            }

            @Override
            public void onError(String mensaje) {
                enviando = false;
                if (cerrada()) return;
                binding.footer.btnRevisar.setAlpha(1f);
                aviso(mensaje + " Intenta de nuevo.");
            }
        });
    }

    private void marcar(Parte p, boolean correcta) {
        for (TextView b : p.botones) {
            boolean esta = ((Integer) b.getTag()) == p.elegida;
            if (!esta) {
                if (correcta) { b.setEnabled(false); b.setAlpha(esSilaba(p) ? 0.5f : 1f); }
                continue;
            }
            b.setAlpha(1f);
            if (esSilaba(p)) {
                b.setBackgroundResource(correcta ? R.drawable.bg_field_green : R.drawable.bg_field_coral);
            } else {
                b.setBackgroundResource(correcta ? R.drawable.bg_opcion_ok : R.drawable.bg_opcion_mal);
            }
        }
        if (correcta) {
            p.resuelta = true;
            if (esSilaba(p)) {                       // escribe la sílaba en el patrón "BA-____"
                JSONObject s = tarjeta.optJSONObject("silabas");
                String silaba = "";
                for (TextView b : p.botones) {
                    if (((Integer) b.getTag()) == p.elegida) silaba = b.getText().toString().replaceFirst("^[a-z]\\)\\s*", "");
                }
                binding.tvPreguntaSilabas.setText(ApiClient.txt(s, "enunciado") + "\n"
                        + ApiClient.txt(s, "patron").replace("____", silaba));
            }
        } else {
            p.elegida = -1;
        }
    }

    // ================================================================ FIN DE PALABRA / NIVEL
    private void palabraTerminada() {
        Feedback.acierto(binding.ivGusanoCabeza);
        JSONObject p = tarjeta.optJSONObject("significadoPregunta");
        String nombre = p == null ? "" : ApiClient.txt(p, "palabra");

        if (repaso) {
            ProgresoApi.cargarResumen(this, (r, e) -> { });
            new AlertDialog.Builder(this)
                    .setTitle("¡Excelente!")
                    .setMessage("Ya dominas la palabra «" + nombre + "». 🌟")
                    .setCancelable(false)
                    .setPositiveButton("Volver", (d, w) -> finish())
                    .show();
            return;
        }

        String significado = ApiClient.txt(tarjeta, "significado");
        boolean ultima = indice + 1 >= palabras.length();
        new AlertDialog.Builder(this)
                .setTitle("¡Muy bien! 🌟")
                .setMessage("Descubriste una palabra nueva:\n\n«" + nombre + "»\n" + significado)
                .setCancelable(false)
                .setPositiveButton(ultima ? "Ver mi resultado ★" : "Siguiente palabra ▶", (d, w) -> {
                    if (ultima) finalizar(); else mostrar(indice + 1);
                })
                .show();
    }

    private void finalizar() {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idUsuario", idUsuario());
            cuerpo.put("ticket", ticket);
            cuerpo.put("nivel", nivel);
            cuerpo.put("actividad", "practica");
            cuerpo.put("aciertos", aciertos);
            cuerpo.put("total", total);
        } catch (JSONException e) {
            return;
        }
        ApiClient.post("palabras/finalizar", cuerpo, new ApiClient.Respuesta() {
            @Override
            public void onExito(JSONObject json) {
                if (cerrada()) return;
                if (!json.optBoolean("ok", false)) {
                    reintentarFinal(json.optString("error", "No pudimos guardar tu resultado"));
                    return;
                }
                ProgresoApi.cargarResumen(PracticaPalabrasActivity.this, (r, e) -> { });
                ResultadoPalabras.mostrar(PracticaPalabrasActivity.this, json, nuevas, PracticaPalabrasActivity.class);
            }

            @Override
            public void onError(String mensaje) {
                if (cerrada()) return;
                reintentarFinal(mensaje);
            }
        });
    }

    private void reintentarFinal(String texto) {
        new AlertDialog.Builder(this)
                .setTitle("Ups")
                .setMessage(texto + ". Toca \"Reintentar\".")
                .setCancelable(false)
                .setPositiveButton("Reintentar", (d, w) -> finalizar())
                .setNegativeButton("Salir", (d, w) -> finish())
                .show();
    }
}
