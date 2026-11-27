package com.example.wordy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.databinding.ItemPalabraDiccionarioBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Tarjetas del "Diccionario del Gusanito" con lo que manda /api/palabras/mis-palabras. */
public class DiccionarioAdapter extends RecyclerView.Adapter<DiccionarioAdapter.VH> {

    public interface Acciones {
        void repasar(JSONObject palabra);
        void escuchar(JSONObject palabra);
    }

    private final List<JSONObject> items = new ArrayList<>();
    private String textoVacio = "";
    private final Acciones acciones;

    public DiccionarioAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    /** Muestra las palabras desbloqueadas; si no hay ninguna, una tarjeta con el mensaje. */
    public void setPalabras(List<JSONObject> palabras, String vacio) {
        items.clear();
        items.addAll(palabras);
        textoVacio = vacio;
        notifyDataSetChanged();
    }

    public boolean esVacio(int posicion) {
        return items.isEmpty() && posicion == 0;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemPalabraDiccionarioBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        if (items.isEmpty()) {
            h.b.tvPalabra.setText("¡Aún no hay palabras!");
            h.b.tvDefinicion.setText(textoVacio);
            h.b.getRoot().setBackgroundResource(R.drawable.bg_card_white);
            h.b.ivIcono.setVisibility(View.GONE);
            h.b.getRoot().setOnClickListener(null);
            h.b.getRoot().setClickable(false);
            return;
        }
        JSONObject p = items.get(position);
        String emoji = ApiClient.txt(p, "emoji");
        h.b.tvPalabra.setText((emoji.isEmpty() ? "" : emoji + " ") + ApiClient.txt(p, "palabra"));
        String ejemplo = ApiClient.txt(p, "ejemplo");
        int veces = p.optInt("vecesRepasada", 0);
        h.b.tvDefinicion.setText(ApiClient.txt(p, "significado")
                + (ejemplo.isEmpty() ? "" : " Ejemplo: " + ejemplo)
                + (veces > 0 ? "\nRepasada " + veces + (veces == 1 ? " vez" : " veces") : ""));

        boolean conImagen = !ApiClient.txt(p, "imagen").isEmpty();
        h.b.getRoot().setBackgroundResource(conImagen ? R.drawable.bg_card_gris : R.drawable.bg_card_white);
        h.b.ivIcono.setVisibility(View.VISIBLE);
        h.b.ivIcono.setImageResource(conImagen ? R.drawable.ic_enlace : R.drawable.ic_toque);
        h.b.ivIcono.setOnClickListener(v -> acciones.escuchar(p));
        h.b.getRoot().setOnClickListener(v -> acciones.repasar(p));
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 1 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPalabraDiccionarioBinding b;

        VH(ItemPalabraDiccionarioBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
