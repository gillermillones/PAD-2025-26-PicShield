package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btnBack; // Aunque en el XML es MaterialButton, aquí Button funciona bien

    private GalleryAdapter adapter;
    private List<Map<String, Object>> activities = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de que este nombre coincide con tu archivo XML de diseño
        setContentView(R.layout.activity_gallery);

        // Enlazamos con la vista
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerGallery);
        progressBar = findViewById(R.id.progressBarGallery);

        // Acción del botón volver
        btnBack.setOnClickListener(v -> finish());

        // Configuración de la lista
        db = FirebaseFirestore.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializamos adaptador
        adapter = new GalleryAdapter(this, activities);
        recyclerView.setAdapter(adapter);

        // Cargar datos
        loadActivities();
    }

    private void loadActivities() {
        // Mostramos la rueda de carga
        progressBar.setVisibility(View.VISIBLE);

        db.collection("activities")
                .orderBy("date") // Ordenar por fecha
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activities.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("id", doc.getId());
                        activity.put("title", doc.getString("title"));
                        // Importante: cogemos la fecha para mostrarla en la tarjeta nueva
                        activity.put("date", doc.getString("date"));

                        activities.add(activity);
                    }
                    adapter.notifyDataSetChanged();

                    // Ocultamos la rueda de carga
                    progressBar.setVisibility(View.GONE);

                    if (activities.isEmpty()) {
                        Toast.makeText(GalleryActivity.this, "No hay actividades disponibles", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GalleryActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}