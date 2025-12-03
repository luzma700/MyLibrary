package com.luz.mylibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ResenasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private DatabaseHelper dbHelper;
    private List<Review> reviewList;
    private FloatingActionButton fabAddReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resenas);

        // Configurar ActionBar por defecto
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reseñas de Libros");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupRecyclerView();
        loadReviews();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewReviews);
        fabAddReview = findViewById(R.id.fabAddReview);

        dbHelper = new DatabaseHelper(this);
        reviewList = new ArrayList<>();
    }

    // Manejar el botón de retroceso de la ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(reviewList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reviewAdapter);
    }

    private void loadReviews() {
        // Obtener usuario actual y cargar solo sus reseñas
        SharedPreferences prefs = getSharedPreferences("MyLibraryPrefs", MODE_PRIVATE);
        String currentUser = prefs.getString("username", "");

        List<Review> reviews = dbHelper.getUserReviewsAsList(currentUser);
        reviewList.clear();
        reviewList.addAll(reviews);
        reviewAdapter.updateList(reviewList);

        if (reviewList.isEmpty()) {
            Toast.makeText(this, "No hay reseñas disponibles", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cargadas " + reviewList.size() + " reseñas", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        fabAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(ResenasActivity.this, SubirResenaActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadReviews();
            Toast.makeText(this, "Reseña agregada correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews();
    }
}