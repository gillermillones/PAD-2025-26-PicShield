package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ViewActivitiesActivity extends AppCompatActivity {

    private LinearLayout activitiesContainer;
    private FirebaseFirestore db;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activities);

        activitiesContainer = findViewById(R.id.activitiesContainer);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        loadActivities();
    }

    private void loadActivities() {
        activitiesContainer.removeAllViews();

        db.collection("activities")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "No hay actividades disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : query) {

                        String activityId = doc.getId();
                        String title = doc.getString("title");
                        String date = doc.getString("date");

                        // Usamos item_activity.xml, que es el que tiene el título y la fecha
                        View card = getLayoutInflater().inflate(R.layout.item_activity, activitiesContainer, false);

                        // Buscamos los TextViews dentro de la tarjeta
                        TextView tvTitle = card.findViewById(R.id.tvActivityTitle);
                        TextView tvDate = card.findViewById(R.id.tvActivityDate);

                        // Ponemos los datos
                        tvTitle.setText(title != null ? title : "Sin Título");
                        tvDate.setText(date != null ? "Fecha: " + date : "");

                        // Hacemos que la tarjeta se pueda pulsar
                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(ViewActivitiesActivity.this, ViewActivityPhotosActivity.class);
                            intent.putExtra("activityId", activityId);
                            startActivity(intent);
                        });

                        // Añadimos la tarjeta a la vista
                        activitiesContainer.addView(card);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando actividades", Toast.LENGTH_SHORT).show();
                });
    }
}