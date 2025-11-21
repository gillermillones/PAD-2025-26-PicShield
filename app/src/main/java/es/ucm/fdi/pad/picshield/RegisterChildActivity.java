package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RegisterChildActivity extends AppCompatActivity {


    private EditText etFirstName, etLastName, etDNI;
    private ImageView ivFacePhoto;
    private CheckBox cbAllowPhotos;
    private Button btnSaveChild;

    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDNI = findViewById(R.id.etDNI);
        ivFacePhoto = findViewById(R.id.ivFacePhoto);
        cbAllowPhotos = findViewById(R.id.cbAllowPhotos);
        btnSaveChild = findViewById(R.id.btnSaveChild);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Selector de imagen
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivFacePhoto.setImageURI(uri);
                    }
                });

        ivFacePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSaveChild.setOnClickListener(v -> saveChild());
    }

    private void saveChild() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dni = etDNI.getText().toString().trim();
        boolean allowPhotos = cbAllowPhotos.isChecked();

        if (firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecciona una foto del niño", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String childId = dni; // Usamos el DNI como ID único
        StorageReference imageRef = storage.getReference()
                .child("children_photos")
                .child(user.getUid())
                .child(childId + ".jpg");

        // Subir foto a Firebase Storage
        imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String photoUrl = uri.toString();

                    // Guardar datos en Firestore
                    Map<String, Object> childMap = new HashMap<>();
                    childMap.put("firstName", firstName);
                    childMap.put("lastName", lastName);
                    childMap.put("dni", dni);
                    childMap.put("photoUrl", photoUrl);
                    childMap.put("allowPhotos", allowPhotos);

                    db.collection("users")
                            .document(user.getUid())
                            .collection("children")
                            .document(childId)
                            .set(childMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RegisterChildActivity.this, "Hijo registrado correctamente", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(RegisterChildActivity.this, "Error al guardar hijo", Toast.LENGTH_SHORT).show());
                })
        ).addOnFailureListener(e ->
                Toast.makeText(RegisterChildActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show());
    }


}
