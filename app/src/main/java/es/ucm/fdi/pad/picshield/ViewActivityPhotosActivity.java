package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewActivityPhotosActivity extends AppCompatActivity {

    private String activityId;
    private LinearLayout previewContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activity_photos);

        db = FirebaseFirestore.getInstance();
        previewContainer = findViewById(R.id.previewContainer);

        activityId = getIntent().getStringExtra("activityId");
        if (activityId == null) {
            Toast.makeText(this, "Error: actividad no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPhotos();
    }

    private void loadPhotos() {
        previewContainer.removeAllViews();

        db.collection("activities")
                .document(activityId)
                .collection("photos")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "No hay fotos subidas todavía", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (var doc : query.getDocuments()) {
                        String url = doc.getString("url");
                        addPhotoToLayout(url);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando fotos", Toast.LENGTH_SHORT).show()
                );
    }

    private void addPhotoToLayout(String url) {
        ImageView iv = new ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setPadding(8, 16, 8, 16);

        Glide.with(this).load(url).into(iv);

        previewContainer.addView(iv);
    }
}
