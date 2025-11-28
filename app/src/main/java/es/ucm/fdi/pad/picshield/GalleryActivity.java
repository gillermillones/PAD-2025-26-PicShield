package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
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
    private Button btnBack;

    private GalleryAdapter adapter;
    private List<Map<String, Object>> activities = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerGallery);
        progressBar = findViewById(R.id.progressBarGallery);

        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GalleryAdapter(this, activities);
        recyclerView.setAdapter(adapter);

        loadActivities();
    }

    private void loadActivities() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        db.collection("activities")
                .orderBy("date") // ordenadas por fecha
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activities.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("id", doc.getId());
                        activity.put("title", doc.getString("title"));
                        activity.put("date", doc.getString("date"));

                        activities.add(activity);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(ProgressBar.GONE);

                    if (activities.isEmpty()) {
                        Toast.makeText(GalleryActivity.this, "No hay actividades", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(GalleryActivity.this, "Error cargando actividades", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
