package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GalleryAdapter adapter;
    private List<String> imageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerGallery);
        progressBar = findViewById(R.id.progressBarGallery);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GalleryAdapter(this, imageUrls);
        recyclerView.setAdapter(adapter);

        loadImagesFromFirebase();
    }

    private void loadImagesFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    fetchDownloadUrls(listResult);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GalleryActivity.this, "Error cargando imágenes", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchDownloadUrls(ListResult listResult) {
        if (listResult.getItems().isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No hay imágenes", Toast.LENGTH_SHORT).show();
            return;
        }

        for (StorageReference item : listResult.getItems()) {
            item.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrls.add(uri.toString());
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });
        }
    }
}

