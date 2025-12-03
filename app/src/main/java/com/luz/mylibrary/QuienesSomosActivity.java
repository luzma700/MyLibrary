package com.luz.mylibrary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class QuienesSomosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quienes_somos);

        // Configurar el botón para volver al inicio
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volver a HomeActivity o MainActivity según tu estructura
                Intent intent = new Intent(QuienesSomosActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Puedes personalizar el título de la ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quiénes Somos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}