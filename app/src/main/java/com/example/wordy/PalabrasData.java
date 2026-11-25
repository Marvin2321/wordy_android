package com.example.wordy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Contenido local de Aprende palabras (nivel 5 = palabras del diseño). */
public final class PalabrasData {

    private PalabrasData() {}

    private static Palabra p(int nivel, String palabra, String corto, String significado,
                             String ejemplo, String silabas, int falta, String d1, String d2, int imagen) {
        return new Palabra("p_" + palabra.toLowerCase(Locale.ROOT), nivel, palabra, corto, significado,
                ejemplo, silabas.split("-"), falta, new String[]{d1, d2}, imagen);
    }

    private static final List<Palabra> TODAS = Arrays.asList(
            // ===== Nivel 1 =====
            p(1, "Pollito", "cría de la gallina", "Cría de la gallina, pequeñita y de plumas amarillas.",
                    "El pollito siguió a su mamá por el corral.", "PO-LLI-TO", 1, "YI", "LI", 0),
            p(1, "Jirafa", "animal de cuello muy largo", "Animal de África con el cuello larguísimo y manchas en el cuerpo.",
                    "La jirafa alcanzó las hojas más altas del árbol.", "JI-RA-FA", 0, "GI", "HI", 0),
            p(1, "Ballena", "gigante del mar", "Animal enorme que vive en el mar y respira por la parte de arriba de su cabeza.",
                    "La ballena saltó fuera del agua.", "BA-LLE-NA", 1, "YE", "LE", 0),
            p(1, "Vaca", "animal que da leche", "Animal de granja que nos da la leche.",
                    "La vaca come pasto en el campo.", "VA-CA", 0, "BA", "FA", 0),
            // ===== Nivel 2 =====
            p(2, "Zapato", "calza el pie", "Prenda que protege y cubre tus pies al caminar.",
                    "Me puse el zapato izquierdo antes de salir.", "ZA-PA-TO", 0, "SA", "CA", 0),
            p(2, "Ventana", "abertura de la pared", "Abertura en la pared por donde entra la luz y el aire.",
                    "Abrí la ventana para ver la lluvia.", "VEN-TA-NA", 0, "BEN", "TEN", 0),
            p(2, "Cebolla", "verdura que hace llorar", "Verdura redonda de varias capas que hace llorar al cortarla.",
                    "Mamá picó la cebolla para la sopa.", "CE-BO-LLA", 2, "YA", "LA", 0),
            p(2, "Hueso", "parte dura del cuerpo", "Pieza dura que forma el esqueleto del cuerpo.",
                    "El perro escondió su hueso en el jardín.", "HUE-SO", 0, "UE", "GUE", 0),
            // ===== Nivel 3 =====
            p(3, "Girasol", "flor amarilla gigante", "Flor grande y amarilla que gira siguiendo al sol.",
                    "El girasol miraba hacia el sol toda la mañana.", "GI-RA-SOL", 0, "JI", "HI", 0),
            p(3, "Llave", "abre puertas", "Objeto de metal que sirve para abrir una puerta.",
                    "Perdí la llave de la casa.", "LLA-VE", 1, "BE", "FE", 0),
            p(3, "Abeja", "insecto que hace miel", "Insecto que vuela de flor en flor y fabrica miel.",
                    "La abeja zumbaba entre las flores.", "A-BE-JA", 2, "GA", "HA", 0),
            p(3, "Cigüeña", "ave de patas largas", "Ave grande de patas largas que hace su nido en lo alto.",
                    "La cigüeña llegó a su nido en la torre.", "CI-GÜE-ÑA", 1, "GUE", "JUE", 0),
            // ===== Nivel 4 =====
            p(4, "Hormiga", "insecto trabajador", "Insecto muy pequeño y trabajador que vive en colonias bajo tierra.",
                    "La hormiga cargó una migaja enorme.", "HOR-MI-GA", 0, "OR", "JOR", 0),
            p(4, "Búho", "ave de la noche", "Ave nocturna de ojos grandes que ulula en la noche.",
                    "El búho observaba desde la rama.", "BÚ-HO", 1, "O", "JO", 0),
            p(4, "Jabalí", "cerdo salvaje", "Animal salvaje parecido a un cerdo, con colmillos.",
                    "Un jabalí cruzó el camino del bosque.", "JA-BA-LÍ", 1, "VA", "PA", 0),
            p(4, "Hielo", "agua congelada", "Agua congelada, dura y muy fría.",
                    "El hielo se derritió con el calor.", "HIE-LO", 0, "IE", "YE", 0),
            // ===== Nivel 5 =====
            p(5, "Baya", "fruto del bosque", "Una fruta pequeña, redonda y muy jugosa que crece en los arbustos del bosque.",
                    "Mi hermana recogió una baya del arbusto y se la comió.", "BA-YA", 1, "LLA", "ILLA", R.drawable.img_rel_baya),
            p(5, "Gazapo", "conejito bebé", "Se dice de un conejito bebé recién nacido, o también de un error pequeño y divertido que alguien comete sin querer al hablar o escribir.",
                    "El abuelo soltó una carcajada cuando cometió un pequeño gazapo al contar la historia al revés.", "GA-ZA-PO", 1, "SA", "CA", R.drawable.img_rel_gazapo),
            p(5, "Ajeno", "que pertenece a otro", "Es todo aquello que no es tuyo, sino que le pertenece a otra persona, a un amigo o a un vecino.",
                    "El ratoncito sabía que no debía tomar un trozo de queso ajeno sin pedirle permiso primero a su mamá.", "A-JE-NO", 1, "GE", "HE", R.drawable.img_rel_ajeno),
            p(5, "Pueril", "propio de niños", "Es todo aquello que es propio de los niños pequeños o que es muy infantil.",
                    "Comer con las manos y llenarse la cara de pastel es un comportamiento pueril.", "PUE-RIL", 1, "RIR", "REL", R.drawable.img_rel_pueril),
            p(5, "Fenicio", "antiguo marinero", "Persona de un pueblo muy antiguo que eran grandes marineros y comerciantes.",
                    "El fenicio navegó hasta tierras lejanas para vender sus telas.", "FE-NI-CIO", 2, "SIO", "ZIO", R.drawable.img_rel_fenicio),
            p(5, "Haber", "existir o estar", "Es una palabra que usamos para explicar que algo existe, que algo ocurrió o que hay cosas guardadas en algún lugar.",
                    "Debajo de la cama de los duendes debe haber muchos juguetes escondidos listos para la Navidad.", "HA-BER", 1, "VER", "PER", 0)
    );

    public static List<Palabra> todas() {
        return TODAS;
    }

    public static List<Palabra> deNivel(int nivel) {
        List<Palabra> lista = new ArrayList<>();
        for (Palabra p : TODAS) if (p.nivel == nivel) lista.add(p);
        return lista;
    }

    public static Palabra porId(String id) {
        for (Palabra p : TODAS) if (p.id.equals(id)) return p;
        return null;
    }

    public static Palabra porNombre(String nombre) {
        for (Palabra p : TODAS) if (p.palabra.equalsIgnoreCase(nombre)) return p;
        return null;
    }
}