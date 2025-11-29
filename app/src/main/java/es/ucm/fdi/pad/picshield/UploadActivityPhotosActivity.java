package es.ucm.fdi.pad.picshield;

import android.graphics.Bitmap;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private FaceManager faceManager;

    // Cola de subida
    private int currentUploadIndex = 0;

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
        faceManager = new FaceManager(this, new FaceApiClient());

        activityId = getIntent().getStringExtra("activityId");
        if (activityId == null) { finish(); return; }

        btnPickImages = findViewById(R.id.btnPickImages);
        btnUpload = findViewById(R.id.btnUploadPhotos);
        btnBack = findViewById(R.id.btnBack);
        previewContainer = findViewById(R.id.previewContainer);

        btnPickImages.setOnClickListener(v -> pickImagesLauncher.launch("image/*"));
        btnUpload.setOnClickListener(v -> startSafeUploadProcess());
        btnBack.setOnClickListener(v -> finish());

        loadExistingPhotos();
    }

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

    // --- PROCESO SEGURO (PIXELADO -> SUBIDA) ---
    private void startSafeUploadProcess() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Selecciona imágenes primero", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpload.setEnabled(false);
        btnUpload.setText("Procesando privacidad...");
        currentUploadIndex = 0;

        processAndUploadNextImage();
    }

    private void processAndUploadNextImage() {
        if (currentUploadIndex >= selectedImages.size()) {
            Toast.makeText(this, "¡Todas las fotos subidas!", Toast.LENGTH_SHORT).show();
            btnUpload.setEnabled(true);
            btnUpload.setText("Subir Fotos");
            selectedImages.clear();
            previewContainer.removeAllViews();
            loadExistingPhotos();
            return;
        }

        Uri currentUri = selectedImages.get(currentUploadIndex);
        File originalFile = FileUtils.getFileFromUri(this, currentUri);

        Toast.makeText(this, "Analizando foto " + (currentUploadIndex + 1) + "...", Toast.LENGTH_SHORT).show();

        // 1. Procesar con Face++
        faceManager.processGroupPhoto(originalFile, new FaceManager.GroupPhotoCallback() {
            @Override
            public void onProcessed(Bitmap bitmap) {
                runOnUiThread(() -> {
                    // 2. Guardar el bitmap (pixelado o no) en un archivo temporal
                    File safeFile = saveBitmapToTempFile(bitmap);
                    if (safeFile != null) {
                        // 3. Subir el archivo seguro a Cloudinary
                        uploadToCloudinary(safeFile);
                    } else {
                        Toast.makeText(UploadActivityPhotosActivity.this, "Error guardando temp", Toast.LENGTH_SHORT).show();
                        currentUploadIndex++; // saltamos esta
                        processAndUploadNextImage();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UploadActivityPhotosActivity.this, "Error API: " + error + ". Subiendo original...", Toast.LENGTH_SHORT).show();
                    // Si falla el pixelado, subimos la original (o podrías abortar)
                    uploadToCloudinary(originalFile);
                });
            }
        });
    }

    private void uploadToCloudinary(File file) {
        MediaManager.get().upload(Uri.fromFile(file))
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
                        runOnUiThread(() -> {
                            Toast.makeText(UploadActivityPhotosActivity.this, "Error Cloudinary", Toast.LENGTH_SHORT).show();
                            currentUploadIndex++;
                            processAndUploadNextImage();
                        });
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void savePhotoInFirestore(String url) {
        Map<String, Object> photoData = new HashMap<>();
        photoData.put("url", url);
        photoData.put("timestamp", System.currentTimeMillis());

        db.collection("activities").document(activityId).collection("photos").add(photoData)
                .addOnSuccessListener(a -> {
                    currentUploadIndex++;
                    processAndUploadNextImage(); // Siguiente foto
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error Firestore", Toast.LENGTH_SHORT).show();
                    currentUploadIndex++;
                    processAndUploadNextImage();
                });
    }

    private File saveBitmapToTempFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "upload_temp_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Métodos loadExistingPhotos y addPhotoToLayout iguales que antes...
    private void loadExistingPhotos() {
        db.collection("activities").document(activityId).collection("photos").orderBy("timestamp").get()
                .addOnSuccessListener(query -> {
                    for (var doc : query.getDocuments()) {
                        addPhotoToLayout(doc.getString("url"));
                    }
                });
    }

    private void addPhotoToLayout(String url) {
        ImageView iv = new ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setPadding(8, 16, 8, 16);
        Glide.with(this).load(url).into(iv);
        previewContainer.addView(iv);
    }
}