package com.luz.mylibrary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.SharedPreferences;


public class MiPerfilActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    private ImageView imgPerfil;
    private TextView tvUsername;
    private EditText etDescripcion;
    private Button btnCambiarFoto, btnTomarFoto, btnGuardar, btnBack;

    private SharedPreferences prefs;
    private String currentPhotoPath = "";
    private String currentCameraPhotoPath = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_perfil);

        initViews();
        loadUserData();
        setupClickListeners();
    }


    private void initViews() {
        imgPerfil = findViewById(R.id.imgPerfil);
        tvUsername = findViewById(R.id.tvUsername);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBack = findViewById(R.id.btnBack);

        prefs = getSharedPreferences("MyLibraryPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        try {
            String username = prefs.getString("username", "Usuario");
            String descripcion = prefs.getString("user_description", "");
            currentPhotoPath = prefs.getString("user_photo_path", "");

            tvUsername.setText(username);
            etDescripcion.setText(descripcion);

            // ⭐⭐ VERIFICACIÓN MEJORADA DE LA FOTO ⭐⭐
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists() && imgFile.length() > 0) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (bitmap != null) {
                        imgPerfil.setImageBitmap(bitmap);
                        Log.d("PERFIL", "Foto cargada correctamente: " + currentPhotoPath);
                    } else {
                        Log.e("PERFIL", "Bitmap es null - archivo corrupto");
                        currentPhotoPath = "";
                        savePhotoPath(""); // Limpiar ruta inválida
                    }
                } else {
                    Log.e("PERFIL", "Archivo no existe o está vacío: " + currentPhotoPath);
                    currentPhotoPath = "";
                    savePhotoPath(""); // Limpiar ruta inválida
                }
            } else {
                Log.d("PERFIL", "No hay foto guardada");
            }

        } catch (Exception e) {
            Log.e("PERFIL", "Error cargando datos: " + e.getMessage());
            Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        btnCambiarFoto.setOnClickListener(v -> openGallery());
        btnTomarFoto.setOnClickListener(v -> checkCameraPermission());
        btnGuardar.setOnClickListener(v -> saveProfile());
        btnBack.setOnClickListener(v -> finish());
    }

    private void openGallery() {
        // Verificar permiso de almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
            return;
        }

        openGalleryIntent();
    }

    private void openGalleryIntent() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, PICK_IMAGE_GALLERY);
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir la galería", Toast.LENGTH_SHORT).show();
            Log.e("PERFIL", "Error galería: " + e.getMessage());
        }
    }

    private void checkCameraPermission() {
        // Verificar permisos de cámara y almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    CAMERA_PERMISSION_REQUEST);
            return;
        }

        takePhoto();
    }

    private void takePhoto() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Log.d("PERFIL", "Archivo temporal creado: " + photoFile.getAbsolutePath());

                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.luz.mylibrary.provider",
                            photoFile);

                    // Dar permisos de lectura a la app de cámara
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(this, "Error creando archivo para foto", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No hay app de cámara disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir la cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PERFIL", "Error cámara: " + e.getMessage(), e);
        }
    }

    private File createImageFile() throws IOException {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // Crear directorio si no existe
            if (storageDir != null && !storageDir.exists()) {
                boolean created = storageDir.mkdirs();
                Log.d("PERFIL", "Directorio Pictures creado: " + created);
            }

            if (storageDir == null) {
                Log.e("PERFIL", "No se pudo acceder al directorio de imágenes");
                return null;
            }

            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );

            currentCameraPhotoPath = image.getAbsolutePath();
            Log.d("PERFIL", "Archivo temporal creado: " + currentCameraPhotoPath);
            return image;

        } catch (Exception e) {
            Log.e("PERFIL", "Error creando archivo: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(this, "Se necesitan permisos de cámara y almacenamiento", Toast.LENGTH_LONG).show();
                }
                break;

            case STORAGE_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryIntent();
                } else {
                    Toast.makeText(this, "Se necesita permiso de almacenamiento", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("PERFIL", "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_GALLERY) {
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    Log.d("PERFIL", "Imagen seleccionada de galería: " + selectedImageUri);
                    processSelectedImage(selectedImageUri, "galería");
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d("PERFIL", "Foto tomada con cámara, procesando...");
                processCameraImage();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.d("PERFIL", "Usuario canceló la operación");
        } else {
            Log.e("PERFIL", "Resultado inesperado: " + resultCode);
        }
    }

    private void processSelectedImage(Uri imageUri, String source) {
        try {
            String savedImagePath = saveImageToInternalStorage(imageUri);
            if (savedImagePath != null) {
                loadAndSetImage(savedImagePath);
                currentPhotoPath = savedImagePath;
                savePhotoPath(currentPhotoPath);
                Toast.makeText(this, "Foto de " + source + " guardada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            Log.e("PERFIL", "Error procesando imagen: " + e.getMessage());
        }
    }

    private void processCameraImage() {
        try {
            Log.d("PERFIL", "Procesando imagen de cámara: " + currentCameraPhotoPath);

            if (currentCameraPhotoPath != null && !currentCameraPhotoPath.isEmpty()) {
                File photoFile = new File(currentCameraPhotoPath);

                if (photoFile.exists()) {
                    Log.d("PERFIL", "Archivo de cámara existe, tamaño: " + photoFile.length());

                    // Verificar si la imagen es válida
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

                    if (options.outWidth > 0 && options.outHeight > 0) {
                        String savedImagePath = saveCameraImageToInternalStorage(photoFile);
                        if (savedImagePath != null) {
                            loadAndSetImage(savedImagePath);
                            currentPhotoPath = savedImagePath;
                            savePhotoPath(currentPhotoPath);
                            Toast.makeText(this, "Foto de cámara guardada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar la foto", Toast.LENGTH_SHORT).show();
                        }

                        // Eliminar archivo temporal
                        photoFile.delete();
                    } else {
                        Toast.makeText(this, "La foto no es válida", Toast.LENGTH_SHORT).show();
                        Log.e("PERFIL", "Imagen inválida - ancho: " + options.outWidth + ", alto: " + options.outHeight);
                    }
                } else {
                    Toast.makeText(this, "No se encontró la foto tomada", Toast.LENGTH_SHORT).show();
                    Log.e("PERFIL", "Archivo no existe: " + currentCameraPhotoPath);
                }
            } else {
                Toast.makeText(this, "Error: ruta de foto no disponible", Toast.LENGTH_SHORT).show();
                Log.e("PERFIL", "Ruta de cámara vacía o nula");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PERFIL", "Error procesando cámara: " + e.getMessage(), e);
        }
    }

    private String saveCameraImageToInternalStorage(File photoFile) {
        FileOutputStream fos = null;
        try {
            // Reducir el tamaño de la imagen para evitar problemas de memoria
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reducir a la mitad

            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

            if (bitmap == null) {
                Log.e("PERFIL", "No se pudo decodificar el bitmap");
                return null;
            }

            File directory = new File(getFilesDir(), "profile_images");
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                Log.d("PERFIL", "Directorio creado: " + created);
            }

            String fileName = "user_profile.jpg";
            File imageFile = new File(directory, fileName);

            fos = new FileOutputStream(imageFile);
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();

            Log.d("PERFIL", "Imagen comprimida: " + compressed + ", guardada en: " + imageFile.getAbsolutePath());

            // Liberar memoria
            bitmap.recycle();

            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e("PERFIL", "Error guardando imagen de cámara: " + e.getMessage(), e);
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e("PERFIL", "Error cerrando stream: " + e.getMessage());
                }
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            File directory = new File(getFilesDir(), "profile_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "user_profile.jpg";
            File imageFile = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e("PERFIL", "Error guardando imagen: " + e.getMessage());
            return null;
        }
    }

    private void loadAndSetImage(String imagePath) {
        try {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imgPerfil.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e("PERFIL", "Error cargando imagen: " + e.getMessage());
        }
    }

    private void savePhotoPath(String photoPath) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_photo_path", photoPath);

            // ⭐⭐ GUARDADO SÍNCRONO Y VERIFICADO ⭐⭐
            boolean saved = editor.commit(); // commit() es síncrono y retorna boolean

            if (saved) {
                Log.d("PERFIL", "Ruta de foto guardada: " + photoPath);
            } else {
                Log.e("PERFIL", "Error: No se pudo guardar la ruta de la foto");
            }
        } catch (Exception e) {
            Log.e("PERFIL", "Exception guardando ruta: " + e.getMessage());
        }
    }

    private void saveProfile() {
        try {
            String descripcion = etDescripcion.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_description", descripcion);

            // ⭐⭐ AGREGAR ESTAS LÍNEAS CRÍTICAS ⭐⭐
            editor.apply(); // Guardado inmediato
            // o usar:
            // editor.commit(); // Para versiones antiguas de Android

            Log.d("PERFIL", "Perfil guardado - Descripción: " + descripcion);
            Log.d("PERFIL", "Ruta foto: " + currentPhotoPath);

            Toast.makeText(this, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("PERFIL", "Error guardando perfil: " + e.getMessage());
            Toast.makeText(this, "Error al guardar perfil", Toast.LENGTH_SHORT).show();
        }
    }
}