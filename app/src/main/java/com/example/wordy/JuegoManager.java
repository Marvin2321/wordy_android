package com.example.wordy;

import java.util.ArrayList;
import java.util.List;

/** Reglas del juego: desbloqueo de niveles y estrellas. */
public class JuegoManager {

    public static final int NIVELES = 5;

    private final JuegoRepository repo;

    public JuegoManager(JuegoRepository repo) {
        this.repo = repo;
    }

    // ===== Cuenta cuentos =====

    public boolean cuentoDesbloqueado(int nivel) {
        return nivel == 1 || repo.cuentoEstrellas(nivel - 1) > 0;
    }

    public int estrellasCuento(int nivel) {
        return repo.cuentoEstrellas(nivel);
    }

    /** Primer cuento sin completar (1..5) o 0 si ya terminó todos. */
    public int siguienteNivelCuentos() {
        for (int n = 1; n <= NIVELES; n++) {
            if (repo.cuentoEstrellas(n) == 0) return n;
        }
        return 0;
    }

    /**
     * Guarda el resultado del cuento. Estrellas: 1 por completarlo, 2 o 3 según los
     * aciertos al primer intento. Solo se guarda si mejora la marca anterior.
     * @return estrellas NUEVAS que hay que sumar al total del niño
     */
    public int guardarCuento(int nivel, int aH, int eH, int totalH, int aP, int eP, int totalP) {
        double r = (aH + aP) / (double) Math.max(1, totalH + totalP);
        int estrellas = r >= 0.8 ? 3 : (r >= 0.5 ? 2 : 1);
        int previas = repo.cuentoEstrellas(nivel);
        if (estrellas > previas) {
            repo.guardarCuento(nivel, estrellas, aH, eH, aP, eP);
        }
        return Math.max(0, estrellas - previas);
    }

    // ===== Aprende palabras =====

    public boolean palabraDesbloqueada(String id) {
        return repo.palabraDesbloqueada(id);
    }

    public void desbloquearPalabra(String id) {
        repo.desbloquearPalabra(id);
    }

    public void repasar(String id) {
        repo.registrarRepaso(id);
    }

    public boolean nivelPalabrasCompleto(int nivel) {
        for (Palabra p : PalabrasData.deNivel(nivel)) {
            if (!repo.palabraDesbloqueada(p.id)) return false;
        }
        return true;
    }

    public boolean nivelPalabrasDesbloqueado(int nivel) {
        return nivel == 1 || nivelPalabrasCompleto(nivel - 1);
    }

    /** Primer nivel de palabras sin completar (1..5) o 0 si ya terminó todos. */
    public int siguienteNivelPalabras() {
        for (int n = 1; n <= NIVELES; n++) {
            if (!nivelPalabrasCompleto(n)) return n;
        }
        return 0;
    }

    public List<Palabra> palabrasDesbloqueadas(int nivel) {
        List<Palabra> lista = new ArrayList<>();
        for (Palabra p : PalabrasData.deNivel(nivel)) {
            if (repo.palabraDesbloqueada(p.id)) lista.add(p);
        }
        return lista;
    }
}