package com.luz.mylibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mylibrary.db";
    private static final int DATABASE_VERSION = 3; // ⬅️ AUMENTA A VERSIÓN 3

    // Tabla usuarios
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Tabla libros - AGREGAR COLUMNA DE USUARIO
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_BOOK_ID = "id";
    public static final String COLUMN_BOOK_TITLE = "title";
    public static final String COLUMN_BOOK_AUTHOR = "author";
    public static final String COLUMN_BOOK_YEAR = "year";
    public static final String COLUMN_BOOK_COVER = "cover";
    public static final String COLUMN_BOOK_USER_OWNER = "user_owner"; // ⭐⭐ NUEVA COLUMNA ⭐⭐

    // Tabla reseñas
    public static final String TABLE_REVIEWS = "reviews";
    public static final String COLUMN_REVIEW_ID = "id";
    public static final String COLUMN_REVIEW_USER = "user";
    public static final String COLUMN_REVIEW_TEXT = "review_text";
    public static final String COLUMN_REVIEW_BOOK_TITLE = "book_title";
    public static final String COLUMN_REVIEW_RATING = "rating";

    // Sentencia SQL para crear tabla de usuarios
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT UNIQUE," +
                    COLUMN_PASSWORD + " TEXT" +
                    ");";

    // Sentencia SQL para crear tabla de libros - ACTUALIZADA
    private static final String CREATE_TABLE_BOOKS =
            "CREATE TABLE " + TABLE_BOOKS + "(" +
                    COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_BOOK_TITLE + " TEXT," +
                    COLUMN_BOOK_AUTHOR + " TEXT," +
                    COLUMN_BOOK_YEAR + " TEXT," +
                    COLUMN_BOOK_COVER + " BLOB," +
                    COLUMN_BOOK_USER_OWNER + " TEXT" + // ⭐⭐ AGREGAR PROPIETARIO ⭐⭐
                    ");";

    // Sentencia SQL para crear tabla de reseñas
    private static final String CREATE_TABLE_REVIEWS =
            "CREATE TABLE " + TABLE_REVIEWS + "(" +
                    COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_REVIEW_USER + " TEXT," +
                    COLUMN_REVIEW_TEXT + " TEXT," +
                    COLUMN_REVIEW_BOOK_TITLE + " TEXT," +
                    COLUMN_REVIEW_RATING + " REAL DEFAULT 0.0" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear todas las tablas
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_BOOKS);
        db.execSQL(CREATE_TABLE_REVIEWS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migración de versión 2 a 3 - Agregar columna user_owner a libros
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_BOOKS + " ADD COLUMN " +
                    COLUMN_BOOK_USER_OWNER + " TEXT DEFAULT 'default_user'");
            Log.d("DatabaseHelper", "Base de datos actualizada a versión 3 - Columna user_owner agregada");
        }

        // Migración de versión 1 a 2 (ya existente)
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_REVIEWS + " ADD COLUMN " +
                    COLUMN_REVIEW_RATING + " REAL DEFAULT 0.0");
            Log.d("DatabaseHelper", "Base de datos actualizada a versión 2 - Columna rating agregada");
        }
    }

    // ==================== MÉTODOS DE USUARIO ====================

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ? AND " +
                COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE " + COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // ==================== MÉTODOS DE LIBROS ====================

    // 🔹 AGREGAR LIBRO CON USUARIO PROPIETARIO
    public boolean insertBook(String title, String author, String year, String userOwner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, title);
        values.put(COLUMN_BOOK_AUTHOR, author);
        values.put(COLUMN_BOOK_YEAR, year);
        values.put(COLUMN_BOOK_USER_OWNER, userOwner); // ⭐⭐ AGREGAR PROPIETARIO ⭐⭐

        long result = db.insert(TABLE_BOOKS, null, values);
        db.close();

        Log.d("DatabaseHelper", "Libro insertado - Owner: " + userOwner + ", Result: " + result);
        return result != -1;
    }

    // 🔹 OBTENER TODOS LOS LIBROS (PARA ADMIN O COMPATIBILIDAD)
    public Cursor getAllBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_YEAR},
                null, null, null, null,
                COLUMN_BOOK_TITLE + " ASC");
    }

    // 🔹 OBTENER LIBROS DEL USUARIO ACTUAL (SEGURIDAD)
    public Cursor getUserBooks(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_YEAR},
                COLUMN_BOOK_USER_OWNER + " = ?", //  FILTRAR POR USUARIO
                new String[]{username},
                null, null,
                COLUMN_BOOK_TITLE + " ASC",  // CORRECCIÓN: ORDER BY aquí
                null); // LIMIT null (sin límite)
    }


    // 🔹 OBTENER LIBROS PARA SPINNER (SOLO DEL USUARIO ACTUAL)
    public Cursor getBooksForSpinner(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_YEAR},
                COLUMN_BOOK_USER_OWNER + " = ?", // FILTRAR POR USUARIO
                new String[]{username},
                null, null,
                COLUMN_BOOK_TITLE + " ASC",  // CORRECCIÓN: ORDER BY aquí
                null); // LIMIT null
    }


    // ==================== MÉTODOS DE RESEÑAS ====================

    public boolean insertReview(String user, String reviewText, String bookTitle, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_USER, user);
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_REVIEW_BOOK_TITLE, bookTitle);
        values.put(COLUMN_REVIEW_RATING, rating);

        long result = db.insert(TABLE_REVIEWS, null, values);
        db.close();

        Log.d("DatabaseHelper", "Reseña insertada - User: " + user + ", Result: " + result);
        return result != -1;
    }

    // 🔹 ELIMINAR RESEÑA CON VERIFICACIÓN DE PROPIEDAD
    public boolean deleteUserReview(int reviewId, String currentUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // SOLO ELIMINAR SI EL USUARIO ES EL PROPIETARIO
            int result = db.delete(TABLE_REVIEWS,
                    COLUMN_REVIEW_ID + " = ? AND " + COLUMN_REVIEW_USER + " = ?",
                    new String[]{String.valueOf(reviewId), currentUser});

            Log.d("DatabaseHelper", "Eliminación revisada - User: " + currentUser + ", Review ID: " + reviewId + ", Result: " + result);
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error eliminando reseña: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // 🔹 OBTENER TODAS LAS RESEÑAS (PARA ADMIN)
    public Cursor getAllReviews() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_REVIEWS,
                null, null, null, null, null,
                COLUMN_REVIEW_ID + " DESC");
    }

    // 🔹 OBTENER RESEÑAS CON INFORMACIÓN DE PROPIEDAD
    public List<Review> getUserReviewsAsList(String currentUser) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_REVIEWS,
                    null, null, null, null, null,
                    COLUMN_REVIEW_ID + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_ID));
                    String user = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_USER));
                    String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_BOOK_TITLE));
                    String reviewText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_TEXT));
                    float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_RATING));

                    // DETERMINAR SI EL USUARIO ACTUAL ES EL PROPIETARIO
                    boolean isOwner = currentUser.equals(user);

                    // Crear review con información de propiedad
                    Review review = new Review(id, bookTitle, "", reviewText, rating, user);
                    review.setOwner(isOwner); // Necesitas agregar este setter en la clase Review
                    reviews.add(review);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error obteniendo reseñas: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        Log.d("DatabaseHelper", "Reseñas cargadas: " + reviews.size() + " para usuario: " + currentUser);
        return reviews;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    public Cursor getBookById(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_YEAR},
                COLUMN_BOOK_ID + " = ?",
                new String[]{String.valueOf(bookId)}, null, null, null);
    }

    public String getBookTitleAuthor(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR},
                COLUMN_BOOK_ID + " = ?",
                new String[]{String.valueOf(bookId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_AUTHOR));
            cursor.close();
            return title + " - " + author;
        }
        if (cursor != null) {
            cursor.close();
        }
        return "";
    }
}