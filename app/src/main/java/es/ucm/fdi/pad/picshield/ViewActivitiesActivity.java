package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ViewActivitiesActivity extends AppCompatActivity {

    private LinearLayout activitiesContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activities);

        activitiesContainer = findViewById(R.id.activitiesContainer);
        db = FirebaseFirestore.getInstance();

        loadActivities();
    }

    private void loadActivities() {
        activitiesContainer.removeAllViews();

        db.collection("activities")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "No hay actividades disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (var doc : query.getDocuments()) {
                        String activityId = doc.getId();
                        String name = doc.getString("name"); // suponer que cada actividad tiene un campo "name"

                        // Crear TextView clickable para cada actividad
                        TextView tv = new TextView(this);
                        tv.setText(name != null ? name : "Actividad sin nombre");
                        tv.setTextSize(18f);
                        tv.setPadding(16, 16, 16, 16);
                        tv.setClickable(true);
                        tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                        tv.setOnClickListener(v -> {
                            // Abrir galería de fotos de esa actividad
                            Intent intent = new Intent(ViewActivitiesActivity.this, ViewActivityPhotosActivity.class);
                            intent.putExtra("activityId", activityId);
                            startActivity(intent);
                        });

                        activitiesContainer.addView(tv);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando actividades", Toast.LENGTH_SHORT).show()
                );
    }
}
