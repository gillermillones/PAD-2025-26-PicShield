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

    private Button btnCreateActivity, btnViewGallery, btnLogout, btnDeleteFaceSet;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FaceManager faceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar FaceManager
        faceManager = new FaceManager(this, new FaceApiClient());

        btnCreateActivity = findViewById(R.id.btnCreateActivity);
        btnViewGallery = findViewById(R.id.btnViewGallery);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteFaceSet = findViewById(R.id.btnDeleteFaceSet);

        // Crear actividad
        btnCreateActivity.setOnClickListener(v -> openCreateActivityDialog());

        // Ver actividades
        btnViewGallery.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        // Borrar Faceset
        btnDeleteFaceSet.setOnClickListener(v -> {
            Toast.makeText(this, "Borrando FaceSet...", Toast.LENGTH_SHORT).show();
            btnDeleteFaceSet.setEnabled(false); // Evitar doble click

            faceManager.deleteFaceSet(new FaceManager.ProcessCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(TeacherActivity.this, message, Toast.LENGTH_LONG).show();
                        btnDeleteFaceSet.setEnabled(true);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(TeacherActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        btnDeleteFaceSet.setEnabled(true);
                    });
                }
            });
        });

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TeacherActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void openCreateActivityDialog() {
        CreateActivityDialog dialog = new CreateActivityDialog();
        dialog.setOnActivityCreatedListener((title, description, date) -> {
            createActivityInFirestore(title, description, date);
        });
        dialog.show(getSupportFragmentManager(), "CreateActivityDialog");
    }

    private void createActivityInFirestore(String title, String description, String date) {
        if(mAuth.getCurrentUser() == null) return;

        Map<String, Object> activityData = new HashMap<>();
        activityData.put("title", title);
        activityData.put("description", description);
        activityData.put("date", date);
        activityData.put("createdBy", mAuth.getCurrentUser().getUid());

        db.collection("activities")
                .add(activityData)
                .addOnSuccessListener(docRef -> {
                    String activityId = docRef.getId();
                    Toast.makeText(TeacherActivity.this, "Actividad creada correctamente", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(TeacherActivity.this, UploadActivityPhotosActivity.class);
                    i.putExtra("activityId", activityId);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeacherActivity.this, "Error creando actividad", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}