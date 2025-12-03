package com.luz.mylibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;
    private DatabaseHelper dbHelper;
    private Context context;

    public ReviewAdapter(List<Review> reviewList, Context context) {
        this.reviewList = new ArrayList<>(reviewList);
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);

        // ⭐⭐ SOLO MOSTRAR BOTÓN ELIMINAR SI ES EL PROPIETARIO ⭐⭐
        if (review.isOwner()) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    deleteReview(adapterPosition, review);
                }
            });
        } else {
            holder.btnEliminar.setVisibility(View.GONE); // OCULTAR BOTÓN
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateList(List<Review> newList) {
        this.reviewList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    private void deleteReview(int position, Review review) {
        try {
            // Validar que la posición sea válida
            if (position < 0 || position >= reviewList.size()) {
                Toast.makeText(context, "Error: posición inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("ReviewAdapter", "Eliminando reseña en posición: " + position + ", ID: " + review.getId());

            // ⭐⭐ OBTENER USUARIO ACTUAL PARA VERIFICACIÓN DE SEGURIDAD ⭐⭐
            SharedPreferences prefs = context.getSharedPreferences("MyLibraryPrefs", Context.MODE_PRIVATE);
            String currentUser = prefs.getString("username", "");

            // ⭐⭐ CORRECCIÓN: Usar deleteUserReview en lugar de deleteReview ⭐⭐
            boolean success = dbHelper.deleteUserReview(review.getId(), currentUser);

            if (success) {
                // Crear nueva lista para evitar problemas de concurrencia
                List<Review> newList = new ArrayList<>(reviewList);
                newList.remove(position);

                // Actualizar la lista principal
                reviewList.clear();
                reviewList.addAll(newList);

                // Notificar al adapter
                notifyItemRemoved(position);

                Toast.makeText(context, "Reseña eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al eliminar de la base de datos", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ReviewAdapter", "Error al eliminar: " + e.getMessage(), e);
            Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookTitle, tvReview, tvReviewer;
        private RatingBar ratingBar;
        private Button btnEliminar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvReview = itemView.findViewById(R.id.tvReview);
            tvReviewer = itemView.findViewById(R.id.tvReviewer);
            ratingBar = itemView.findViewById(R.id.ratingBarItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void bind(Review review) {
            tvBookTitle.setText(review.getBookTitle());
            tvReview.setText(review.getReviewText());
            tvReviewer.setText("Por: " + review.getReviewerName());

            // Configurar rating
            float rating = review.getRating();
            ratingBar.setRating(rating);
        }
    }
}