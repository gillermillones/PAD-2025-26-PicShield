package es.ucm.fdi.pad.picshield;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivityPhotosActivity extends AppCompatActivity {

    private String activityId;
    private Button btnPickImages, btnUpload, btnBack;
    private LinearLayout previewContainer;

    private final List<Uri> selectedImages = new ArrayList<>();
    private FirebaseFirestore db;

    // Launcher para seleccionar múltiples imágenes
    private final ActivityResultLauncher<String> pickImagesLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    selectedImages.clear();
                    selectedImages.addAll(uris);
                    showPreviewImages();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_activity_photos);

        db = FirebaseFirestore.getInstance();
        activityId = getIntent().getStringExtra("activityId");

        if (activityId == null) {
            Toast.makeText(this, "Error: actividad no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnPickImages = findViewById(R.id.btnPickImages);
        btnUpload = findViewById(R.id.btnUploadPhotos);
        btnBack = findViewById(R.id.btnBack);
        previewContainer = findViewById(R.id.previewContainer);

        // Seleccionar imágenes
        btnPickImages.setOnClickListener(v -> pickImagesLauncher.launch("image/*"));

        // Subir imágenes
        btnUpload.setOnClickListener(v -> uploadAllImages());

        // Botón volver
        btnBack.setOnClickListener(v -> finish());

        // Cargar fotos ya subidas
        loadExistingPhotos();
    }

    /**
     * Carga fotos ya guardadas en Firestore
     */
    private void loadExistingPhotos() {
        previewContainer.removeAllViews();

        db.collection("activities")
                .document(activityId)
                .collection("photos")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    for (var doc : query.getDocuments()) {
                        String url = doc.getString("url");
                        addPhotoToLayout(url);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando fotos", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Añade una imagen (de URL Cloudinary) al layout vertical
     */
    private void addPhotoToLayout(String url) {
        ImageView iv = new ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setPadding(8, 16, 8, 16);

        Glide.with(this).load(url).into(iv);

        previewContainer.addView(iv);
    }

    /**
     * Muestra miniaturas de las imágenes seleccionadas antes de subirlas
     */
    private void showPreviewImages() {
        previewContainer.removeAllViews();

        for (Uri imgUri : selectedImages) {
            ImageView iv = new ImageView(this);
            iv.setImageURI(imgUri);
            iv.setAdjustViewBounds(true);
            iv.setMaxHeight(350);
            iv.setPadding(8, 16, 8, 16);

            previewContainer.addView(iv);
        }
    }

    /**
     * Sube todas las imágenes seleccionadas a Cloudinary
     */
    private void uploadAllImages() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Selecciona imágenes primero", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Subiendo fotos...", Toast.LENGTH_SHORT).show();

        for (Uri img : selectedImages) {
            uploadSingleImage(img);
        }
    }

    /**
     * Sube una sola imagen
     */
    private void uploadSingleImage(Uri imgUri) {
        MediaManager.get().upload(imgUri)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = resultData.get("secure_url").toString();
                        savePhotoInFirestore(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(UploadActivityPhotosActivity.this,
                                "Error subiendo imagen", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    /**
     * Guarda la URL de una foto en Firestore y la muestra automáticamente
     */
    private void savePhotoInFirestore(String url) {
        Map<String, Object> photoData = new HashMap<>();
        photoData.put("url", url);
        photoData.put("timestamp", System.currentTimeMillis());

        db.collection("activities")
                .document(activityId)
                .collection("photos")
                .add(photoData)
                .addOnSuccessListener(a -> addPhotoToLayout(url))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error guardando foto", Toast.LENGTH_SHORT).show()
                );
    }
}
