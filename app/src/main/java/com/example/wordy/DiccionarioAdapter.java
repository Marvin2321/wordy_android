package com.example.wordy;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordy.databinding.ItemPalabraDiccionarioBinding;

import java.util.List;

public class DiccionarioAdapter extends RecyclerView.Adapter<DiccionarioAdapter.VH> {

    /** Modelo mínimo; cuando llegue la lógica se reemplaza por los datos reales. */
    public static class Item {
        final String palabra;
        final String definicion;
        final boolean conEnlace; // true = tarjeta gris con ícono de enlace

        public Item(String palabra, String definicion, boolean conEnlace) {
            this.palabra = palabra;
            this.definicion = definicion;
            this.conEnlace = conEnlace;
        }
    }

    private final List<Item> items;

    public DiccionarioAdapter(List<Item> items) {
        this.items = items;
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
                it.conEnlace ? R.drawable.bg_card_gris : R.drawable.bg_card_white);
        h.b.ivIcono.setImageResource(
                it.conEnlace ? R.drawable.ic_enlace : R.drawable.ic_toque);
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