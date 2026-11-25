package com.example.wordy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.databinding.ItemPalabraDiccionarioBinding;

import java.util.ArrayList;
import java.util.List;

public class DiccionarioAdapter extends RecyclerView.Adapter<DiccionarioAdapter.VH> {

    public interface OnPalabraClick {
        void onClick(String idPalabra);
    }

    private static class Item {
        final String id, palabra, definicion;
        final boolean conImagen, vacio;

        Item(String id, String palabra, String definicion, boolean conImagen, boolean vacio) {
            this.id = id;
            this.palabra = palabra;
            this.definicion = definicion;
            this.conImagen = conImagen;
            this.vacio = vacio;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private final OnPalabraClick listener;

    public DiccionarioAdapter(OnPalabraClick listener) {
        this.listener = listener;
    }

    /** Muestra las palabras; si no hay ninguna, un mensaje de "aún no hay palabras". */
    public void setPalabras(List<Palabra> palabras) {
        items.clear();
        for (Palabra p : palabras) {
            items.add(new Item(p.id, p.palabra, p.significado + " Ejemplo: " + p.ejemplo,
                    p.imagen != 0, false));
        }
        if (palabras.isEmpty()) {
            items.add(new Item(null, "¡Aún no hay palabras!",
                    "Aún no has desbloqueado palabras en este nivel. ¡Sigue jugando!", false, true));
        }
        notifyDataSetChanged();
    }

    public boolean esVacio(int posicion) {
        return posicion >= 0 && posicion < items.size() && items.get(posicion).vacio;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemPalabraDiccionarioBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = items.get(position);
        h.b.tvPalabra.setText(it.palabra);
        h.b.tvDefinicion.setText(it.definicion);
        h.b.getRoot().setBackgroundResource(
                it.conImagen ? R.drawable.bg_card_gris : R.drawable.bg_card_white);

        if (it.vacio) {
            h.b.ivIcono.setVisibility(View.GONE);
            h.b.getRoot().setOnClickListener(null);
            h.b.getRoot().setClickable(false);
        } else {
            h.b.ivIcono.setVisibility(View.VISIBLE);
            h.b.ivIcono.setImageResource(it.conImagen ? R.drawable.ic_enlace : R.drawable.ic_toque);
            h.b.getRoot().setOnClickListener(v -> listener.onClick(it.id));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPalabraDiccionarioBinding b;

        VH(ItemPalabraDiccionarioBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}