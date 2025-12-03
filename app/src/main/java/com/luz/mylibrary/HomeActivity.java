package com.luz.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    // CORREGIDO: Los primeros tres son LinearLayout, solo logout es Button
    LinearLayout btnMiPerfil, btnMiBiblioteca, btnResenas, btnQuienesSomos;
    Button btnLogout;
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // INICIALIZACIÓN CORREGIDA - usa LinearLayout donde corresponda
        tvWelcome = findViewById(R.id.tvWelcome);
        btnMiPerfil = (LinearLayout) findViewById(R.id.btnMiPerfil);        // LinearLayout
        btnMiBiblioteca = (LinearLayout) findViewById(R.id.btnMiBiblioteca); // LinearLayout
        btnResenas = (LinearLayout) findViewById(R.id.btnResenas);          // LinearLayout
        btnQuienesSomos = (LinearLayout) findViewById(R.id.btnQuienesSomos); // LinearLayout
        btnLogout = (Button) findViewById(R.id.btnLogout);                  // Button

        // Mostrar usuario actual
        SharedPreferences prefs = getSharedPreferences("MyLibraryPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Usuario");
        tvWelcome.setText("¡Bienvenido, " + username + "! 👋");

        // Navegación a módulos
        btnMiPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MiPerfilActivity.class));
            }
        });

        btnMiBiblioteca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MiBibliotecaActivity.class));
            }
        });

        btnResenas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ResenasActivity.class));
            }
        });

        btnQuienesSomos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, QuienesSomosActivity.class));
            }
        });

        // Cerrar sesión
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpiar datos de sesión
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Toast.makeText(HomeActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

                // Volver al Login
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Evitar que al presionar "Atrás" vuelva al Login
        moveTaskToBack(true);
    }
}