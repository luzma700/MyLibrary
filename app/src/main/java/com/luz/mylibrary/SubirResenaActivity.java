package com.luz.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class SubirResenaActivity extends AppCompatActivity {

    private Spinner spinnerLibros;
    private EditText etReview;
    private RatingBar ratingBar;
    private Button btnSaveReview;

    private DatabaseHelper dbHelper;
    private List<Book> bookList;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_resena);

        try {
            dbHelper = new DatabaseHelper(this);
            getCurrentUser();
            initViews();
            loadBooksFromDatabase();
            setupListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void getCurrentUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyLibraryPrefs", Context.MODE_PRIVATE);
        currentUser = sharedPreferences.getString("username", "Usuario Anónimo");
        Log.d("SubirResena", "Usuario actual: " + currentUser);
    }

    private void initViews() {
        spinnerLibros = findViewById(R.id.spinnerLibros);
        etReview = findViewById(R.id.etReview);
        ratingBar = findViewById(R.id.ratingBar);
        btnSaveReview = findViewById(R.id.btnSaveReview);
    }

    private void loadBooksFromDatabase() {
        bookList = new ArrayList<>();

        try {
            // ⭐⭐ IMPORTANTE: Usar getBooksForSpinner que filtra por usuario ⭐⭐
            Cursor cursor = dbHelper.getBooksForSpinner(currentUser);

            // DEBUG
            int bookCount = cursor != null ? cursor.getCount() : 0;
            Log.d("SubirResena", "Libros para " + currentUser + ": " + bookCount);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_ID));
                    String id = String.valueOf(idInt);
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_TITLE));
                    String author = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_AUTHOR));

                    Book book = new Book(id, title, author, "");
                    bookList.add(book);

                    Log.d("SubirResena", "✓ " + title + " - " + author);

                } while (cursor.moveToNext());
                cursor.close();
            }

            // Crear adapter
            ArrayAdapter<Book> adapter = new ArrayAdapter<Book>(this,
                    android.R.layout.simple_spinner_item, bookList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    Book book = getItem(position);
                    if (book != null) {
                        textView.setText(book.getTitle() + " - " + book.getAuthor());
                    }
                    return textView;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                    Book book = getItem(position);
                    if (book != null) {
                        textView.setText(book.getTitle() + " - " + book.getAuthor());
                    }
                    return textView;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLibros.setAdapter(adapter);

            if (bookList.isEmpty()) {
                Toast.makeText(this, "No tienes libros en tu biblioteca. Agrega libros primero.", Toast.LENGTH_LONG).show();
                btnSaveReview.setEnabled(false);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar libros: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("SubirResena", "Error: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        btnSaveReview.setOnClickListener(v -> saveReview());
    }

    private void saveReview() {
        try {
            if (bookList.isEmpty()) {
                Toast.makeText(this, "No hay libros disponibles", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPosition = spinnerLibros.getSelectedItemPosition();
            if (selectedPosition < 0) {
                Toast.makeText(this, "Selecciona un libro", Toast.LENGTH_SHORT).show();
                return;
            }

            Book selectedBook = bookList.get(selectedPosition);
            String reviewText = etReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (validateInput(reviewText)) {
                boolean success = dbHelper.insertReview(currentUser, reviewText, selectedBook.getTitle(), rating);

                if (success) {
                    Toast.makeText(this, "Reseña guardada exitosamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar la reseña", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("SubirResena", "Error: " + e.getMessage(), e);
        }
    }

    private boolean validateInput(String review) {
        if (review.isEmpty()) {
            etReview.setError("La reseña no puede estar vacía");
            return false;
        }
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, SubirResenaActivity.class);
    }
}