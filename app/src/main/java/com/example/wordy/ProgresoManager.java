package com.example.wordy;

import java.util.Calendar;

public class ProgresoManager {

    private final ProgresoRepository repo;
    private final String usuarioId;

    public ProgresoManager(ProgresoRepository repo, String usuarioId) {
        this.repo = repo;
        this.usuarioId = usuarioId;
    }

    /** Carga el progreso; si pasó más de un día sin actividad, la racha vuelve a 0. */
    public Usuario cargar() {
        Usuario u = repo.obtener(usuarioId);
        if (u.ultimoDia >= 0 && diaActual() - u.ultimoDia > 1 && u.racha != 0) {
            u.racha = 0;
            repo.guardar(u);
        }
        return u;
    }

    /** @param correcta true si la hizo bien (suma 1 estrella) */
    public Usuario registrarActividad(boolean correcta) {
        return registrarActividad(correcta ? 1 : 0);
    }

    /**
     * Llamar al terminar una actividad.
     * @param estrellasGanadas estrellas nuevas a sumar (puede ser 0: igual cuenta para la racha)
     */
    public Usuario registrarActividad(int estrellasGanadas) {
        Usuario u = cargar();
        long hoy = diaActual();

        if (u.ultimoDia != hoy) {                       // primera actividad del día
            u.racha = (u.ultimoDia == hoy - 1) ? u.racha + 1 : 1;
            u.ultimoDia = hoy;
        }
        u.estrellas += Math.max(0, estrellasGanadas);

        repo.guardar(u);
        return u;
    }

    /** Número de día local (días desde 1970), compatible con minSdk 24. */
    static long diaActual() {
        Calendar c = Calendar.getInstance();
        long ms = c.getTimeInMillis()
                + c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
        return ms / 86_400_000L;
    }
}