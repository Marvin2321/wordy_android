package com.example.wordy;
import android.util.Patterns;

public class Validador {
    private Validador() {}

    /** Devuelve el mensaje de error, o null si todo está bien. */
    public static String validarLogin(int tipoCuentaPos, String usuario, String contrasena) {
        if (tipoCuentaPos == 0) return "Elige tu tipo de cuenta";
        if (usuario == null || usuario.trim().isEmpty()) return "Escribe tu usuario";
        if (contrasena == null || contrasena.length() < 4)
            return "La contraseña debe tener al menos 4 caracteres";
        return null;
    }
    /** Devuelve el mensaje de error, o null si todo está bien. */
    public static String validarRegistro(int tipoCuentaPos, String correo, String usuario, String contrasena) {
        if (tipoCuentaPos == 0) return "Elige tu tipo de cuenta";
        if (correo == null || !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches())
            return "Escribe un correo válido";
        if (usuario == null || usuario.trim().length() < 3)
            return "El usuario debe tener al menos 3 caracteres";
        if (contrasena == null || contrasena.length() < 4)
            return "La contraseña debe tener al menos 4 caracteres";
        return null;
    }
}
