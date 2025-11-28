package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TeacherActivity extends AppCompatActivity {

    private Button btnCreateActivity, btnViewGallery, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnCreateActivity = findViewById(R.id.btnCreateActivity);
        btnViewGallery = findViewById(R.id.btnViewGallery);
        btnLogout = findViewById(R.id.btnLogout);

        // --- CREAR ACTIVIDAD ---
        btnCreateActivity.setOnClickListener(v -> openCreateActivityDialog());

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

    /**
     * Abre el diálogo para crear actividades
     */
    private void openCreateActivityDialog() {
        CreateActivityDialog dialog = new CreateActivityDialog();

        dialog.setOnActivityCreatedListener((title, description, date) -> {
            createActivityInFirestore(title, description, date);
        });

        dialog.show(getSupportFragmentManager(), "CreateActivityDialog");
    }

    /**
     * Crea la actividad en Firestore y muestra mensaje
     */
    private void createActivityInFirestore(String title, String description, String date) {

        Map<String, Object> activityData = new HashMap<>();
        activityData.put("title", title);
        activityData.put("description", description);
        activityData.put("date", date);
        activityData.put("createdBy", mAuth.getCurrentUser().getUid());

        db.collection("activities")
                .add(activityData)
                .addOnSuccessListener(docRef -> {
                    String activityId = docRef.getId();

                    Toast.makeText(TeacherActivity.this,
                            "Actividad creada correctamente", Toast.LENGTH_SHORT).show();

                    // Abrimos la pantalla para subir fotos
                    Intent i = new Intent(TeacherActivity.this, UploadActivityPhotosActivity.class);
                    i.putExtra("activityId", activityId);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeacherActivity.this,
                            "Error creando actividad", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
