package com.example.wordy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.wordy.databinding.ActivityDiccionarioBinding;

import java.util.ArrayList;
import java.util.List;

public class DiccionarioActivity extends AppCompatActivity {

    private ActivityDiccionarioBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiccionarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.btnMenu.setOnClickListener(v -> Navegacion.mostrarMenu(this));
        binding.footer.btnHome.setOnClickListener(v -> Navegacion.irAHome(this));
        binding.footer.btnRegresar.setOnClickListener(v -> finish());

        binding.rvPalabras.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvPalabras.setAdapter(new DiccionarioAdapter(datosDeEjemplo()));
    }

    /** Datos de muestra para ver el diseño. Luego vendrán de la base de datos. */
    private List<DiccionarioAdapter.Item> datosDeEjemplo() {
        List<DiccionarioAdapter.Item> l = new ArrayList<>();
        l.add(new DiccionarioAdapter.Item("Gazapo",
                "Se dice de un conejito bebé recién nacido, o también de un error pequeño y divertido que alguien comete sin querer al hablar o escribir. Ejemplo: El abuelo soltó una carcajada cuando cometió un pequeño gazapo al contar la historia al revés.", true));
        l.add(new DiccionarioAdapter.Item("Pueril",
                "Es todo aquello que es propio de los niños pequeños o que es muy infantil. Ejemplo: Comer con las manos y llenarse la cara de pastel es un comportamiento pueril.", false));
        l.add(new DiccionarioAdapter.Item("Ajeno",
                "Es todo aquello que no es tuyo, sino que le pertenece a otra persona, a un amigo o a un vecino. Ejemplo: El ratoncito sabía que no debía tomar un trozo de queso ajeno sin pedirle permiso primero a su mamá.", true));
        l.add(new DiccionarioAdapter.Item("Haber",
                "Es una palabra que usamos para explicar que algo existe, que algo ocurrió o que hay cosas guardadas en algún lugar. Ejemplo: Debajo de la cama de los duendes debe haber muchos juguetes escondidos listos para la Navidad.", false));
        l.add(new DiccionarioAdapter.Item("Fenicio",
                "Se refiere a las personas de un pueblo muy antiguo que eran grandes marineros y comerciantes.", false));
        l.add(new DiccionarioAdapter.Item("Baya",
                "Es una fruta pequeña, redonda y muy jugosa que crece en los arbustos del bosque.", false));
        return l;
    }
}