package com.example.wordy;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Hace que la pantalla se pueda desplazar y que, al abrir el teclado,
 * el campo que se está llenando quede visible.
 */
public final class KeyboardScroll {

    private KeyboardScroll() {}

    public static void configurar(Activity activity, ScrollView scroll,
                                  ConstraintLayout contenido, View... campos) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        // El diseño usa porcentajes de alto: se le da una altura FIJA (la de la
        // pantalla, con un mínimo) para que no se estire ni se deforme cuando
        // aparece el teclado. Si no cabe, se hace scroll.
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int minimo = (int) (640 * dm.density);
        ViewGroup.LayoutParams lp = contenido.getLayoutParams();
        lp.height = Math.max(dm.heightPixels, minimo);
        contenido.setLayoutParams(lp);

        // Al aparecer el teclado, la zona visible se reduce y se hace scroll.
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, ime.bottom);
            View foco = activity.getCurrentFocus();
            if (ime.bottom > 0 && foco != null) {
                scroll.post(() -> irA(scroll, foco));
            }
            return insets;
        });

        // Al pasar de un campo a otro, lleva el campo activo a la vista.
        for (View campo : campos) {
            campo.setOnFocusChangeListener((v, tieneFoco) -> {
                if (tieneFoco) scroll.postDelayed(() -> irA(scroll, v), 250);
            });
        }
    }

    private static void irA(ScrollView scroll, View campo) {
        Rect r = new Rect();
        campo.getDrawingRect(r);
        scroll.offsetDescendantRectToMyCoords(campo, r);
        int y = r.top - scroll.getHeight() / 3;
        scroll.smoothScrollTo(0, Math.max(0, y));
    }
}