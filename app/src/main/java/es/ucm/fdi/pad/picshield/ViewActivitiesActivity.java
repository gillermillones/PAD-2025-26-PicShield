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
    private Button btnBack; // Aunque en el XML sea MaterialButton, aquí lo tratamos como Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activities);

        activitiesContainer = findViewById(R.id.activitiesContainer);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        // Botón volver
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
                        // 1. Recuperamos la fecha (asegúrate de que en Firebase se llame "date")
                        String date = doc.getString("date");

                        // 2. EN LUGAR DE 'NEW BUTTON', INFLAMOS EL DISEÑO BONITO
                        // Usamos item_activity.xml, que es el que tiene el título y la fecha
                        View card = getLayoutInflater().inflate(R.layout.item_activity, activitiesContainer, false);

                        // 3. BUSCAMOS LOS TEXTVIEWS DENTRO DE LA TARJETA
                        TextView tvTitle = card.findViewById(R.id.tvActivityTitle);
                        TextView tvDate = card.findViewById(R.id.tvActivityDate);

                        // 4. PONEMOS LOS DATOS
                        tvTitle.setText(title != null ? title : "Sin Título");
                        tvDate.setText(date != null ? "Fecha: " + date : "");

                        // 5. HACEMOS QUE LA TARJETA SE PUEDA PULSAR
                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(ViewActivitiesActivity.this, ViewActivityPhotosActivity.class);
                            intent.putExtra("activityId", activityId);
                            startActivity(intent);
                        });

                        // 6. AÑADIMOS LA TARJETA A LA LISTA
                        activitiesContainer.addView(card);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando actividades", Toast.LENGTH_SHORT).show();
                });
    }
}