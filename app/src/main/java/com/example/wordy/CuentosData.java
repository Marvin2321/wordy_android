package com.example.wordy;

import java.util.Arrays;
import java.util.List;

/** Contenido local de Cuenta cuentos. Cuando haya API, esto se reemplaza por consultas. */
public final class CuentosData {

    private CuentosData() {}

    private static Cuento.Escena e(String texto) {
        return new Cuento.Escena(texto, null, "");
    }

    private static Cuento.Escena h(String antes, String ok, String mal1, String mal2, String despues) {
        return new Cuento.Escena(antes, new Cuento.Hueco(ok, new String[]{ok, mal1, mal2}), despues);
    }

    private static Cuento.Pregunta q(String enunciado, int correcta, String a, String b, String c) {
        return new Cuento.Pregunta(enunciado, new String[]{a, b, c}, correcta);
    }

    private static final List<Cuento> TODOS = Arrays.asList(

            new Cuento(1, "Los Tres Cerditos", Arrays.asList(
                    h("Había una vez tres cerditos que vivían con su mamá en una ", "casa", "mesa", "cama",
                            ". Un día, mamá les dijo: “Ya son grandes. Hagan su propia casa”."),
                    e("El primer cerdito construyó una casita de paja. El segundo, una casita de madera. El tercero, con mucho esfuerzo, levantó una casa de piedra."),
                    e("Un día llegó un lobo grande y dijo frente a la casa de paja: “¡Abre la casa!”. El cerdito respondió: “¡No!”. Entonces el lobo sopló y sopló, y la casa de paja cayó."),
                    e("El cerdito corrió a la casa de madera. El lobo también llegó, sopló y sopló, y la casa de madera cayó. Los dos cerditos corrieron a la casa de piedra con su hermano."),
                    e("El lobo llegó y sopló con mucha fuerza, pero la casa de piedra no cayó. El lobo se fue y los tres cerditos se quedaron juntos, muy felices.")
            ), Arrays.asList(
                    q("¿Cuántos cerditos había?", 1, "Dos", "Tres", "Cuatro"),
                    q("¿Qué animal llegó a las casas?", 2, "Un perro", "Un gato", "Un lobo"),
                    q("¿Qué casa no cayó?", 2, "La de paja", "La de madera", "La de piedra")
            ), R.drawable.img_tres_casas, R.drawable.img_tres_cerditos),

            new Cuento(2, "La Liebre y la Tortuga", Arrays.asList(
                    h("Una liebre presumía de ser la más veloz del bosque y se burlaba de la tortuga porque caminaba muy ",
                            "lento", "alto", "frío", "."),
                    e("—Te reto a una carrera —dijo la tortuga. La liebre aceptó entre carcajadas."),
                    h("Al empezar, la liebre salió disparada y dejó muy atrás a la tortuga. Confiada, se echó a ",
                            "dormir", "volar", "cantar", " bajo un árbol."),
                    e("La tortuga siguió caminando, paso a paso, sin detenerse. Cuando la liebre despertó, la tortuga ya estaba cruzando la meta."),
                    e("Desde ese día, la liebre aprendió que la constancia vale más que la velocidad.")
            ), Arrays.asList(
                    q("¿Quién se burlaba de la tortuga?", 0, "La liebre", "El lobo", "El gato"),
                    q("¿Qué hizo la liebre durante la carrera?", 1, "Comió", "Se durmió", "Cantó"),
                    q("¿Quién ganó la carrera?", 2, "La liebre", "Nadie", "La tortuga")
            ), 0, 0),

            new Cuento(3, "El León y el Ratón", Arrays.asList(
                    h("Un león dormía bajo un gran árbol cuando un ratoncito pasó corriendo sobre su ",
                            "cabeza", "mesa", "zapato", ". El león despertó muy enojado."),
                    e("—Perdóname, rey de la selva —suplicó el ratón—. Algún día te ayudaré. El león soltó una carcajada, pero lo dejó ir."),
                    h("Semanas después, unos cazadores atraparon al león con una ",
                            "red", "silla", "taza", " muy fuerte."),
                    h("El león rugía sin poder escapar. El ratón lo oyó, llegó corriendo y empezó a ",
                            "morder", "cantar", "pintar", " las cuerdas con sus dientecitos."),
                    e("Poco a poco las cuerdas se rompieron y el león quedó libre. Desde ese día fueron grandes amigos.")
            ), Arrays.asList(
                    q("¿Quién despertó al león?", 1, "Un cazador", "Un ratoncito", "Un elefante"),
                    q("¿Con qué atraparon al león?", 0, "Con una red", "Con una caja", "Con una piedra"),
                    q("¿Cómo ayudó el ratón al león?", 2, "Cantándole", "Trayéndole comida", "Mordiendo las cuerdas")
            ), 0, 0),

            new Cuento(4, "El Patito Feo", Arrays.asList(
                    h("En una granja, una pata empolló varios huevos. El último en abrirse dio un patito grande y gris, y los demás se burlaban de él por ser ",
                            "diferente", "veloz", "pequeño", "."),
                    h("Triste, el patito se fue de la granja. Pasó el otoño solo y el invierno fue muy ",
                            "frío", "dulce", "ruidoso", ", pero sobrevivió."),
                    h("Cuando llegó la primavera, vio en un lago unos cisnes blancos y se acercó con ",
                            "miedo", "hambre", "sueño", "."),
                    h("Al mirar su reflejo en el agua, descubrió que ya no era un patito feo: ¡era un ",
                            "cisne", "gato", "pez", " hermoso!"),
                    e("Los otros cisnes lo recibieron con alegría y nunca más se sintió solo.")
            ), Arrays.asList(
                    q("¿Por qué se burlaban del patito?", 0, "Porque era diferente", "Porque era ruidoso", "Porque era muy pequeño"),
                    q("¿En qué se convirtió?", 1, "Un ganso", "Un cisne", "Un pato blanco"),
                    q("¿Cómo se sintió al final?", 2, "Solo y triste", "Enojado", "Acompañado y feliz")
            ), 0, 0),

            new Cuento(5, "Caperucita Roja", Arrays.asList(
                    h("Caperucita Roja vivía cerca del bosque. Un día su mamá le pidió que llevara una canasta con ",
                            "pan", "piedras", "zapatos", " a su abuelita, que estaba enferma."),
                    h("En el camino se encontró con un lobo, que le preguntó con voz ",
                            "amable", "dormida", "azul", ": —¿A dónde vas, niña?"),
                    h("Caperucita le contó que iba a casa de su abuelita. El lobo corrió por un atajo, llegó primero y se escondió en la ",
                            "cama", "escuela", "calle", " de la abuelita."),
                    h("Cuando Caperucita llegó, notó que su abuelita tenía unas orejas muy ",
                            "grandes", "pequeñas", "verdes", ", y se asustó."),
                    h("De pronto, un leñador que pasaba por ahí escuchó los gritos y corrió a ayudar. El lobo huyó del bosque para siempre y Caperucita y su abuelita lo abrazaron con ",
                            "alegría", "tristeza", "enojo", ". Desde ese día, Caperucita tuvo mucho cuidado en el camino.")
            ), Arrays.asList(
                    q("¿Qué llevaba Caperucita en la canasta?", 0, "Pan", "Flores", "Libros"),
                    q("¿Dónde se escondió el lobo?", 1, "En el bosque", "En la cama de la abuelita", "En el río"),
                    q("¿Quién ayudó a Caperucita?", 2, "Un pescador", "Un maestro", "Un leñador")
            ), 0, 0)
    );

    public static Cuento porNivel(int nivel) {
        for (Cuento c : TODOS) if (c.nivel == nivel) return c;
        return null;
    }
}