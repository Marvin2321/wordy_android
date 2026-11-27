package com.example.wordy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/**
 * Base de todas las pantallas del juego: envuelve el contenido en un NavigationDrawer
 * y conecta el menú (btnMenu), el home (btnHome) y "Regresar" (btnRegresar).
 * Ya no guarda avance local: todo se pide a la API (misma BD que la web).
 */
public abstract class BaseWordyActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private NavigationView navView;

    private final OnBackPressedCallback cerrarDrawer = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            drawer.closeDrawer(GravityCompat.START);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SesionManager.usuarioId(this) == null) {
            irALogin();
        }
    }

    /** true si no hay sesión (la pantalla ya se está cerrando y no debe seguir). */
    protected boolean sinSesion() {
        return SesionManager.usuarioId(this) == null;
    }

    protected String idUsuario() {
        return SesionManager.usuarioId(this);
    }

    /** Para callbacks de red: no tocar vistas si la pantalla ya se cerró. */
    protected boolean cerrada() {
        return isFinishing() || isDestroyed();
    }

    protected void aviso(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setContentView(View vista) {
        drawer = (DrawerLayout) getLayoutInflater().inflate(R.layout.base_drawer, null);
        FrameLayout contenedor = drawer.findViewById(R.id.drawerContenido);
        contenedor.addView(vista, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.setContentView(drawer);

        navView = drawer.findViewById(R.id.navView);
        configurarDrawer();

        enlazar(vista, R.id.btnMenu, v -> drawer.openDrawer(GravityCompat.START));
        enlazar(vista, R.id.btnHome, v -> Navegacion.irAHome(this));
        enlazar(vista, R.id.btnRegresar, v -> finish());
    }

    private void enlazar(View raiz, int id, View.OnClickListener l) {
        View v = raiz.findViewById(id);
        if (v != null) v.setOnClickListener(l);
    }

    private void configurarDrawer() {
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                cerrarDrawer.setEnabled(true);
                actualizarMenuPorRol();
                actualizarEncabezado();
                ProgresoApi.cargarResumen(BaseWordyActivity.this, (r, e) -> {
                    if (!cerrada()) actualizarEncabezado();
                });
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                cerrarDrawer.setEnabled(false);
            }
        });
        getOnBackPressedDispatcher().addCallback(this, cerrarDrawer);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawer.closeDrawer(GravityCompat.START);
            if (id == R.id.menu_inicio) Navegacion.irAHome(this);
            else if (id == R.id.menu_cuentos) ir(CuentaCuentosActivity.class);
            else if (id == R.id.menu_palabras) ir(AprendePalabrasActivity.class);
            else if (id == R.id.menu_diccionario) ir(DiccionarioActivity.class);
            else if (id == R.id.menu_control_parental) ir(ControlParentalActivity.class);
            else if (id == R.id.menu_cerrar_sesion) {
                SesionManager.cerrar(this);
                irALogin();
            }
            return true;
        });

        actualizarMenuPorRol();
        actualizarEncabezado();
    }

    private void actualizarMenuPorRol() {
        MenuItem itemParental = navView.getMenu().findItem(R.id.menu_control_parental);
        if (itemParental != null) {
            itemParental.setVisible(SesionManager.esTutor(this));
        }
    }

    private void actualizarEncabezado() {
        View h = navView.getHeaderView(0);
        String nombre = SesionManager.nombre(this);
        ((TextView) h.findViewById(R.id.tvNavNombre)).setText(nombre.isEmpty()
                ? getString(R.string.nav_hola_anonimo) : getString(R.string.nav_hola, nombre));
        ((TextView) h.findViewById(R.id.tvNavProgreso)).setText(getString(R.string.nav_progreso,
                SesionManager.racha(this), SesionManager.estrellas(this)));
    }

    private void ir(Class<?> destino) {
        if (getClass() == destino) return;
        startActivity(new Intent(this, destino));
        finish();
    }

    private void irALogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    protected int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
