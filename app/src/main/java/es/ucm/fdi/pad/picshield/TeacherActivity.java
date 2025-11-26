package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class TeacherActivity extends AppCompatActivity {

    private Button btnUploadGroupPhoto, btnCreateActivity, btnViewGallery, btnLogout;
    private ImageView imgPreview;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri;

    // Para escoger imagen de la galería
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        btnUploadGroupPhoto = findViewById(R.id.btnUploadGroupPhoto);
        btnCreateActivity = findViewById(R.id.btnCreateActivity);
        btnViewGallery = findViewById(R.id.btnViewGallery);
        btnLogout = findViewById(R.id.btnLogout);
        imgPreview = findViewById(R.id.imgPreview);

        // --- SUBIR FOTO GRUPAL ---
        btnUploadGroupPhoto.setOnClickListener(v -> {
            pickImage.launch("image/*");
        });

        // Cuando se pulsa mantener
        imgPreview.setOnClickListener(v -> {
            if (selectedImageUri != null) uploadGroupPhoto();
        });

        // --- CREAR ACTIVIDAD ---
        btnCreateActivity.setOnClickListener(v -> {
            openCreateActivityDialog();
        });

        // --- VER GALERÍA ---
        btnViewGallery.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        // --- LOGOUT ---
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TeacherActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void uploadGroupPhoto() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "groupPhotos/" + System.currentTimeMillis() + ".jpg";
        StorageReference photoRef = storageRef.child(fileName);

        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(task -> {
                    photoRef.getDownloadUrl().addOnSuccessListener(url -> {

                        // Guardar en Firestore referencia a la foto
                        Map<String, Object> photoData = new HashMap<>();
                        photoData.put("url", url.toString());
                        photoData.put("type", "group");

                        db.collection("photos")
                                .add(photoData)
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error subiendo foto", Toast.LENGTH_SHORT).show();
                });
    }

    private void openCreateActivityDialog() {
        CreateActivityDialog dialog = new CreateActivityDialog();
        dialog.show(getSupportFragmentManager(), "CreateActivityDialog");
    }
}

