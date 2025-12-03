package com.luz.mylibrary;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText etUsername, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLoginLink;
    DatabaseHelper dbHelper; // 🔹 Debería reconocerlo ahora

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🔹 INICIALIZAR DatabaseHelper
        dbHelper = new DatabaseHelper(this);


        // Inicializar vistas
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Botón de registro
        btnRegister.setOnClickListener(v -> registerUser());

        // Enlace para ir al Login
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validaciones
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 4) {
            Toast.makeText(this, "El usuario debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etConfirmPassword.setText("");
            etConfirmPassword.requestFocus();
            return;
        }

        // Verificar si el usuario ya existe
        if (dbHelper.checkUserExists(username)) {
            Toast.makeText(this, "El usuario '" + username + "' ya existe", Toast.LENGTH_SHORT).show();
            etUsername.setText("");
            etUsername.requestFocus();
            return;
        }

        // Registrar nuevo usuario
        boolean success = dbHelper.insertUser(username, password);

        if (success) {
            Toast.makeText(this, "¡Registro exitoso! Ahora puedes iniciar sesión", Toast.LENGTH_SHORT).show();

            // Pasar el username al LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error en el registro. Intenta nuevamente", Toast.LENGTH_SHORT).show();
        }
    }
}