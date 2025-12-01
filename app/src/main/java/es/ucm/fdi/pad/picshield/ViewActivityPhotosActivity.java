package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewActivityPhotosActivity extends AppCompatActivity {

    private String activityId;
    private RecyclerView recyclerPhotos;
    private FirebaseFirestore db;
    private Button btnBack;

    private PhotosAdapter adapter;

    private List<Object> photoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activity_photos);

        db = FirebaseFirestore.getInstance();

        recyclerPhotos = findViewById(R.id.recyclerPhotos);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        activityId = getIntent().getStringExtra("activityId");

        if (activityId == null) {
            Toast.makeText(this, "Error: actividad no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        loadPhotos();
    }

    private void setupRecyclerView() {
        // Configuramos la cuadrícula de 2 columnas
        recyclerPhotos.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new PhotosAdapter(this, photoList);
        recyclerPhotos.setAdapter(adapter);
    }

    private void loadPhotos() {
        db.collection("activities")
                .document(activityId)
                .collection("photos")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "No hay fotos subidas todavía", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    photoList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String url = doc.getString("url");
                        if (url != null) {
                            photoList.add(url);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando fotos", Toast.LENGTH_SHORT).show();
                });
    }
}