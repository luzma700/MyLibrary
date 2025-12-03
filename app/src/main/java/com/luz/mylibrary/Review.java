package com.luz.mylibrary;

public class Review {
    private int id;
    private String bookTitle;
    private String author;
    private String reviewText;
    private float rating;
    private String reviewerName;
    private String date;
    private boolean isOwner; // ⭐⭐ CAMPO AGREGADO ⭐⭐

    public Review() {
        // Constructor vacío
    }

    // Constructor sin ID
    public Review(String bookTitle, String author, String reviewText, float rating, String reviewerName) {
        this.bookTitle = bookTitle;
        this.author = author;
        this.reviewText = reviewText;
        this.rating = rating;
        this.reviewerName = reviewerName;
        this.date = "Hoy";
        this.isOwner = false; // ⭐⭐ VALOR POR DEFECTO ⭐⭐
    }

    // Constructor con ID
    public Review(int id, String bookTitle, String author, String reviewText, float rating, String reviewerName) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.author = author;
        this.reviewText = reviewText;
        this.rating = rating;
        this.reviewerName = reviewerName;
        this.date = "Hoy";
        this.isOwner = false; // ⭐⭐ VALOR POR DEFECTO ⭐⭐
    }

    // ⭐⭐ NUEVO CONSTRUCTOR CON PROPIEDAD ⭐⭐
    public Review(int id, String bookTitle, String author, String reviewText, float rating, String reviewerName, boolean isOwner) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.author = author;
        this.reviewText = reviewText;
        this.rating = rating;
        this.reviewerName = reviewerName;
        this.date = "Hoy";
        this.isOwner = isOwner;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // ⭐⭐ GETTER Y SETTER PARA isOwner ⭐⭐
    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        this.isOwner = owner;
    }

    // ⭐⭐ MÉTODO toString ÚTIL PARA DEBUGGING ⭐⭐
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", bookTitle='" + bookTitle + '\'' +
                ", reviewerName='" + reviewerName + '\'' +
                ", rating=" + rating +
                ", isOwner=" + isOwner +
                '}';
    }
}