package com.luz.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin, btnRegister;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);


        // Inicializar vistas (IDs deben coincidir con el XML)
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Recibir username del registro si viene
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            String receivedUsername = intent.getStringExtra("username");
            etUsername.setText(receivedUsername);
            etPassword.requestFocus();
        }

        // Botón Login
        btnLogin.setOnClickListener(v -> loginUser());

        // Botón Register
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkUserLogin(username, password)) {
            // Guardar sesión
            SharedPreferences prefs = getSharedPreferences("MyLibraryPrefs", MODE_PRIVATE);
            prefs.edit().putString("username", username).putBoolean("isLoggedIn", true).apply();

            Toast.makeText(this, "¡Bienvenido " + username + "!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            etPassword.setText("");
        }
    }
}