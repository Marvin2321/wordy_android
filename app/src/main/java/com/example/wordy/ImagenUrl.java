package com.example.wordy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Descarga imágenes de la BD (Cloudinary u otra URL) y las pone en un ImageView.
 * Sin librerías: HttpURLConnection + caché en memoria. Si falla, deja la imagen de respaldo.
 */
public final class ImagenUrl {

    private static final ExecutorService HILOS = Executors.newFixedThreadPool(3);
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(16 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private ImagenUrl() {}

    /**
     * @param ruta     url_imagen de la BD (puede ser relativa o null)
     * @param respaldo drawable a mostrar mientras carga o si no hay imagen (0 = ocultar el ImageView)
     */
    public static void cargar(ImageView iv, String ruta, int respaldo) {
        String url = ApiClient.urlCompleta(ruta);
        iv.setTag(R.id.tag_url_imagen, url);
        if (url == null) {
            if (respaldo == 0) {
                iv.setVisibility(View.GONE);
            } else {
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(respaldo);
            }
            return;
        }
        iv.setVisibility(View.VISIBLE);
        Bitmap enCache = CACHE.get(url);
        if (enCache != null) {
            iv.setImageBitmap(enCache);
            return;
        }
        if (respaldo != 0) iv.setImageResource(respaldo); else iv.setImageDrawable(null);

        HILOS.execute(() -> {
            Bitmap bmp = descargar(url);
            ApiClient.enPrincipal(() -> {
                // el ImageView pudo reciclarse para otra imagen mientras descargaba
                if (!url.equals(iv.getTag(R.id.tag_url_imagen))) return;
                if (bmp != null) {
                    CACHE.put(url, bmp);
                    iv.setImageBitmap(bmp);
                } else if (respaldo == 0) {
                    iv.setVisibility(View.GONE);
                }
            });
        });
    }

    private static Bitmap descargar(String url) {
        for (int intento = 0; intento < 2; intento++) {     // 1 reintento, como la web
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(url).openConnection();
                con.setConnectTimeout(15_000);
                con.setReadTimeout(30_000);
                if (con.getResponseCode() >= 400) return null;
                byte[] datos;
                try (InputStream in = con.getInputStream()) {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                    datos = out.toByteArray();
                }
                // Reduce imágenes grandes (Cloudinary puede mandar 2000 px) para no gastar memoria
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(datos, 0, datos.length, op);
                int escala = 1;
                while (op.outWidth / (escala * 2) >= 700 && op.outHeight / (escala * 2) >= 700) escala *= 2;
                op = new BitmapFactory.Options();
                op.inSampleSize = escala;
                Bitmap b = BitmapFactory.decodeByteArray(datos, 0, datos.length, op);
                if (b != null) return b;
            } catch (Exception e) {
                // reintenta una vez
            } finally {
                if (con != null) con.disconnect();
            }
        }
        return null;
    }
}
