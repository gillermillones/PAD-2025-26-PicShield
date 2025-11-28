package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ActivityPhotosActivity extends AppCompatActivity {

    private String activityId;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PhotosAdapter adapter;
    private Button btnAddPhotos, btnBack;
    private List<String> photoUrls = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        db = FirebaseFirestore.getInstance();

        activityId = getIntent().getStringExtra("activityId");
        if (activityId == null) {
            Toast.makeText(this, "Error: actividad no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerPhotos);
        progressBar = findViewById(R.id.progressBarPhotos);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new PhotosAdapter(this, photoUrls);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnAddPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadActivityPhotosActivity.class);
            intent.putExtra("activityId", activityId);
            startActivity(intent);
        });

        loadPhotos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhotos(); // Recargar cada vez que vuelva (por si añadió fotos)
    }

    private void loadPhotos() {
        progressBar.setVisibility(View.VISIBLE);
        photoUrls.clear();

        db.collection("activities")
                .document(activityId)
                .collection("photos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    progressBar.setVisibility(View.GONE);

                    for (DocumentSnapshot doc : query) {
                        String url = doc.getString("url");
                        if (url != null) photoUrls.add(url);
                    }

                    adapter.notifyDataSetChanged();

                    if (photoUrls.isEmpty()) {
                        Toast.makeText(this, "No hay fotos en esta actividad", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error cargando fotos", Toast.LENGTH_SHORT).show();
                });
    }
}
