package com.example.wordy;

import android.animation.ObjectAnimator;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/** Sonido + animación de acierto / error (sin archivos de audio). */
public final class Feedback {

    private Feedback() {}

    public static void acierto(View v) {
        sonido(ToneGenerator.TONE_PROP_ACK);
        v.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                .start();
    }

    public static void error(View v) {
        sonido(ToneGenerator.TONE_PROP_NACK);
        ObjectAnimator.ofFloat(v, "translationX", 0f, -14f, 14f, -10f, 10f, 0f)
                .setDuration(350).start();
    }

    private static void sonido(int tono) {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
            tg.startTone(tono, 200);
            new Handler(Looper.getMainLooper()).postDelayed(tg::release, 500);
        } catch (RuntimeException ignorada) {
            // algunos dispositivos no permiten ToneGenerator: se omite el sonido
        }
    }
}