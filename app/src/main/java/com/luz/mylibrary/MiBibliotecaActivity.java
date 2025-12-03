package com.luz.mylibrary;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiBibliotecaActivity extends AppCompatActivity {

    // Búsqueda API
    EditText etBuscarLibro;
    Button btnBuscar;
    Spinner spinnerLibros;
    Button btnGuardarLibro;

    // Lista de libros guardados
    ListView listViewLibrosGuardados;
    Button btnBack;

    List<Book> librosEncontrados = new ArrayList<>();
    List<Book> librosGuardados = new ArrayList<>();
    ArrayAdapter<Book> adapterGuardados;

    DatabaseHelper dbHelper;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_biblioteca);

        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Inicializar vistas de búsqueda
        etBuscarLibro = findViewById(R.id.etBuscarLibro);
        btnBuscar = findViewById(R.id.btnBuscar);
        spinnerLibros = findViewById(R.id.spinnerLibros);
        btnGuardarLibro = findViewById(R.id.btnGuardarLibro);

        // Inicializar lista de libros guardados
        listViewLibrosGuardados = findViewById(R.id.listViewLibrosGuardados);
        btnBack = findViewById(R.id.btnBack);

        // Configurar spinner inicial
        setupSpinnerInicial();

        // Cargar libros guardados
        cargarLibrosGuardados();

        // Botón buscar libros en API
        btnBuscar.setOnClickListener(v -> {
            buscarLibrosAPI();
        });

        // Botón guardar libro seleccionado
        btnGuardarLibro.setOnClickListener(v -> {
            guardarLibroSeleccionado();
        });

        // Botón volver
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Configurar adapter para libros guardados con botón eliminar
        adapterGuardados = new ArrayAdapter<Book>(this,
                R.layout.item_libro_guardado, librosGuardados) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_libro_guardado, parent, false);
                }

                Book book = getItem(position);
                if (book != null) {
                    TextView tvTitulo = convertView.findViewById(R.id.tvTitulo);
                    TextView tvAutor = convertView.findViewById(R.id.tvAutor);
                    TextView tvAnio = convertView.findViewById(R.id.tvAnio);
                    TextView tvGeneros = convertView.findViewById(R.id.tvGeneros);
                    Button btnEliminar = convertView.findViewById(R.id.btnEliminar);

                    tvTitulo.setText(book.getTitle());
                    tvAutor.setText("Autor: " + book.getAuthor());
                    tvAnio.setText("Año: " + book.getYear());
                    tvGeneros.setText("Género: " + book.getGeneros());

                    // Botón eliminar
                    btnEliminar.setOnClickListener(v -> {
                        eliminarLibro(position);
                    });

                    // Click en el item para ver información completa
                    convertView.setOnClickListener(v -> {
                        mostrarInfoCompleta(book);
                    });
                }
                return convertView;
            }
        };
        listViewLibrosGuardados.setAdapter(adapterGuardados);

        // Click largo para eliminar (opcional)
        listViewLibrosGuardados.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                eliminarLibro(position);
                return true;
            }
        });
    }

    private void setupSpinnerInicial() {
        librosEncontrados.clear();
        librosEncontrados.add(new Book("0", "🔍 Busca un libro primero", ""));

        ArrayAdapter<Book> adapter = new ArrayAdapter<Book>(this,
                android.R.layout.simple_spinner_item, librosEncontrados) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLibros.setAdapter(adapter);
    }

    private void buscarLibrosAPI() {
        String query = etBuscarLibro.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Ingresa el título de un libro", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Buscando: " + query, Toast.LENGTH_SHORT).show();

        Call<JsonObject> call = apiService.searchBookByTitle(query);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procesarRespuestaAPI(response.body());
                } else {
                    Toast.makeText(MiBibliotecaActivity.this, "Error en la búsqueda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MiBibliotecaActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 MÉTODO PROCESAR RESPUESTA API - COMPLETO CON TODOS LOS DATOS
    private void procesarRespuestaAPI(JsonObject response) {
        try {
            JsonArray docs = response.getAsJsonArray("docs");
            librosEncontrados.clear();
            librosEncontrados.add(new Book("0", "Selecciona un libro para guardar", ""));

            if (docs != null && docs.size() > 0) {
                int maxResults = Math.min(docs.size(), 15); // Más resultados

                for (int i = 0; i < maxResults; i++) {
                    JsonObject bookObj = docs.get(i).getAsJsonObject();

                    // 🔹 1. OBTENER TÍTULO
                    String title = "Título desconocido";
                    if (bookObj.has("title") && !bookObj.get("title").isJsonNull()) {
                        title = bookObj.get("title").getAsString();
                    }

                    // 🔹 2. OBTENER AUTOR (puede tener múltiples autores)
                    String author = "Autor desconocido";
                    if (bookObj.has("author_name") && bookObj.get("author_name").isJsonArray()) {
                        JsonArray authors = bookObj.getAsJsonArray("author_name");
                        if (authors != null && authors.size() > 0) {
                            StringBuilder autores = new StringBuilder();
                            for (int j = 0; j < Math.min(authors.size(), 3); j++) { // Máximo 3 autores
                                if (!authors.get(j).isJsonNull()) {
                                    if (autores.length() > 0) autores.append(", ");
                                    autores.append(authors.get(j).getAsString());
                                }
                            }
                            author = autores.toString();
                        }
                    }

                    // 🔹 3. OBTENER ID DEL LIBRO (para link)
                    String bookId = "";
                    if (bookObj.has("key") && !bookObj.get("key").isJsonNull()) {
                        bookId = bookObj.get("key").getAsString();
                    }

                    // 🔹 4. OBTENER AÑO DE PUBLICACIÓN
                    String year = "Año desconocido";
                    if (bookObj.has("first_publish_year") && !bookObj.get("first_publish_year").isJsonNull()) {
                        year = String.valueOf(bookObj.get("first_publish_year").getAsInt());
                    } else if (bookObj.has("publish_year") && bookObj.get("publish_year").isJsonArray()) {
                        JsonArray publishYears = bookObj.getAsJsonArray("publish_year");
                        if (publishYears != null && publishYears.size() > 0 && !publishYears.get(0).isJsonNull()) {
                            year = publishYears.get(0).getAsString();
                        }
                    }

                    // 🔹 5. OBTENER GÉNEROS/TEMAS
                    String generos = "Género no especificado";
                    if (bookObj.has("subject") && bookObj.get("subject").isJsonArray()) {
                        JsonArray subjects = bookObj.getAsJsonArray("subject");
                        if (subjects != null && subjects.size() > 0) {
                            StringBuilder generosBuilder = new StringBuilder();
                            for (int j = 0; j < Math.min(subjects.size(), 3); j++) { // Máximo 3 géneros
                                if (!subjects.get(j).isJsonNull()) {
                                    if (generosBuilder.length() > 0) generosBuilder.append(", ");
                                    generosBuilder.append(subjects.get(j).getAsString());
                                }
                            }
                            generos = generosBuilder.toString();
                        }
                    }

                    // 🔹 6. OBTENER URL DE PORTADA
                    String coverUrl = "";
                    if (bookObj.has("cover_i") && !bookObj.get("cover_i").isJsonNull()) {
                        String coverId = bookObj.get("cover_i").getAsString();
                        coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg"; // Imagen grande
                    }

                    // 🔹 7. OBTENER LINK COMPLETO DEL LIBRO
                    String bookLink = "";
                    if (!bookId.isEmpty()) {
                        bookLink = "https://openlibrary.org" + bookId;
                    }

                    // 🔹 8. OBTENER ISBN (si existe)
                    String isbn = "ISBN no disponible";
                    if (bookObj.has("isbn") && bookObj.get("isbn").isJsonArray()) {
                        JsonArray isbns = bookObj.getAsJsonArray("isbn");
                        if (isbns != null && isbns.size() > 0 && !isbns.get(0).isJsonNull()) {
                            isbn = isbns.get(0).getAsString();
                        }
                    }

                    // 🔹 9. OBTENER NÚMERO DE PÁGINAS
                    String paginas = "Páginas no especificadas";
                    if (bookObj.has("number_of_pages_median") && !bookObj.get("number_of_pages_median").isJsonNull()) {
                        paginas = bookObj.get("number_of_pages_median").getAsString() + " páginas";
                    }

                    // 🔹 CREAR OBJETO BOOK CON TODOS LOS DATOS
                    Book book = new Book(bookId, title, author, year);
                    book.setCoverUrl(coverUrl);
                    book.setBookLink(bookLink);
                    book.setGeneros(generos);
                    book.setIsbn(isbn);
                    book.setPaginas(paginas);

                    librosEncontrados.add(book);
                }

                actualizarSpinner();
                Toast.makeText(this, "Encontrados " + (librosEncontrados.size() - 1) + " libros", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se encontraron libros", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error procesando resultados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void actualizarSpinner() {
        ArrayAdapter<Book> adapter = new ArrayAdapter<Book>(this,
                android.R.layout.simple_spinner_item, librosEncontrados) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLibros.setAdapter(adapter);
    }

    private void guardarLibroSeleccionado() {
        Book libroSeleccionado = (Book) spinnerLibros.getSelectedItem();

        if (libroSeleccionado == null || libroSeleccionado.getId().equals("0")) {
            Toast.makeText(this, "Selecciona un libro de la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el libro ya está guardado
        if (libroYaGuardado(libroSeleccionado.getTitle())) {
            Toast.makeText(this, "Este libro ya está en tu biblioteca", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⭐⭐ OBTENER USUARIO ACTUAL PARA GUARDAR EL LIBRO ⭐⭐
        SharedPreferences prefs = getSharedPreferences("MyLibraryPrefs", MODE_PRIVATE);
        String currentUser = prefs.getString("username", "");

        // Guardar en la base de datos CON USUARIO PROPIETARIO
        boolean result = dbHelper.insertBook(
                libroSeleccionado.getTitle(),
                libroSeleccionado.getAuthor(),
                libroSeleccionado.getYear(),
                currentUser  // ⭐⭐ AGREGAR USUARIO PROPIETARIO ⭐⭐
        );

        // ⭐⭐ CORRECCIÓN: insertBook ahora retorna boolean, no long ⭐⭐
        if (result) {
            Toast.makeText(this, "✅ Libro guardado: " + libroSeleccionado.getTitle(), Toast.LENGTH_SHORT).show();
            // Actualizar la lista de libros guardados
            cargarLibrosGuardados();
            // Limpiar búsqueda
            etBuscarLibro.setText("");
            setupSpinnerInicial();
        } else {
            Toast.makeText(this, "Error al guardar el libro", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean libroYaGuardado(String titulo) {
        for (Book libro : librosGuardados) {
            if (libro.getTitle().equals(titulo)) {
                return true;
            }
        }
        return false;
    }

    private void cargarLibrosGuardados() {
        librosGuardados.clear();

        // ⭐⭐ OBTENER USUARIO ACTUAL Y FILTRAR POR USUARIO ⭐⭐
        SharedPreferences prefs = getSharedPreferences("MyLibraryPrefs", MODE_PRIVATE);
        String currentUser = prefs.getString("username", "");

        // Usar getUserBooks en lugar de getAllBooks
        Cursor cursor = dbHelper.getUserBooks(currentUser);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_TITLE));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_AUTHOR));

                String year = "";
                try {
                    int yearColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_YEAR);
                    if (yearColumnIndex != -1) {
                        year = cursor.getString(yearColumnIndex);
                    }
                } catch (Exception e) {
                    Log.e("MiBiblioteca", "Error obteniendo año: " + e.getMessage());
                    year = "Año desconocido";
                }

                Book book = new Book(String.valueOf(id), title, author, year);
                librosGuardados.add(book);

                Log.d("MiBiblioteca", "Libro cargado: " + title + " - Año: " + year + " - Usuario: " + currentUser);
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Log.d("MiBiblioteca", "No hay libros para el usuario: " + currentUser);
        }

        // Actualizar el adapter
        if (adapterGuardados != null) {
            adapterGuardados.notifyDataSetChanged();
        }

        Toast.makeText(this, "Tienes " + librosGuardados.size() + " libros en tu biblioteca", Toast.LENGTH_SHORT).show();
    }


    // 🔹 MÉTODO PARA ELIMINAR LIBRO
    private void eliminarLibro(int position) {
        if (position >= 0 && position < librosGuardados.size()) {
            Book libroAEliminar = librosGuardados.get(position);

            // Mostrar confirmación
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Eliminar Libro")
                    .setMessage("¿Estás seguro de que quieres eliminar \"" + libroAEliminar.getTitle() + "\" de tu biblioteca?")
                    .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                        // Eliminar de la base de datos
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        int result = db.delete(DatabaseHelper.TABLE_BOOKS,
                                DatabaseHelper.COLUMN_BOOK_ID + " = ?",
                                new String[]{libroAEliminar.getId()});
                        db.close();

                        if (result > 0) {
                            // Eliminar de la lista local
                            librosGuardados.remove(position);
                            adapterGuardados.notifyDataSetChanged();
                            Toast.makeText(this, "🗑️ Libro eliminado: " + libroAEliminar.getTitle(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al eliminar el libro", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    // 🔹 MÉTODO PARA MOSTRAR INFORMACIÓN COMPLETA
    private void mostrarInfoCompleta(Book book) {
        String infoCompleta = "📖 " + book.getTitle() + "\n" +
                "✍️ " + book.getAuthor() + "\n" +
                "📅 " + book.getYear() + "\n" +
                "🏷️ " + book.getGeneros() + "\n" +
                "🔢 " + book.getIsbn() + "\n" +
                "📄 " + book.getPaginas() + "\n" +
                "🔗 " + book.getBookLink();

        new android.app.AlertDialog.Builder(this)
                .setTitle("📖 Información Completa")
                .setMessage(infoCompleta)
                .setPositiveButton("Abrir en Navegador", (dialog, which) -> {
                    // Abrir link del libro en navegador
                    if (book.getBookLink() != null && !book.getBookLink().isEmpty() && !book.getBookLink().equals("")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getBookLink()));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(this, "No hay link disponible", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }
}