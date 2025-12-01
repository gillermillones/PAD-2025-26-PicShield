package es.ucm.fdi.pad.picshield;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private RecyclerView recyclerPhotos;

    private final List<Uri> selectedImages = new ArrayList<>();

    private final List<Object> displayList = new ArrayList<>();
    private PhotosAdapter adapter;

    private FirebaseFirestore db;
    private FaceManager faceManager;
    private int currentUploadIndex = 0;

    private final ActivityResultLauncher<String> pickImagesLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    selectedImages.clear();
                    boolean hasInvalidFormat = false;

                    for (Uri uri : uris) {
                        String mimeType = getContentResolver().getType(uri);
                        if (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg"))) {
                            selectedImages.add(uri);
                        } else {
                            hasInvalidFormat = true;
                        }
                    }

                    if (hasInvalidFormat) {
                        Toast.makeText(this, "Aviso: Algunas imágenes no eran JPG y se han descartado.", Toast.LENGTH_LONG).show();
                    }

                    if (!selectedImages.isEmpty()) {
                        showPreviewImages();
                    } else if (hasInvalidFormat) {
                        Toast.makeText(this, "Error: Ninguna imagen seleccionada es JPG.", Toast.LENGTH_LONG).show();
                    }
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

        // Configuración del RecyclerView (Cuadrícula de 2 columnas)
        recyclerPhotos = findViewById(R.id.recyclerPhotos);
        recyclerPhotos.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new PhotosAdapter(this, displayList);
        recyclerPhotos.setAdapter(adapter);

        btnPickImages.setOnClickListener(v -> pickImagesLauncher.launch("image/*"));
        btnUpload.setOnClickListener(v -> startSafeUploadProcess());
        btnBack.setOnClickListener(v -> finish());

        loadExistingPhotos();
    }

    // Muestra las fotos locales seleccionadas
    private void showPreviewImages() {
        displayList.clear();
        displayList.addAll(selectedImages);
        adapter.notifyDataSetChanged();
    }

    // Carga las fotos ya subidas (Firebase)
    private void loadExistingPhotos() {
        db.collection("activities").document(activityId).collection("photos")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        displayList.clear();
                        for (var doc : query.getDocuments()) {
                            // Añadimos las URLs (Strings)
                            String url = doc.getString("url");
                            if (url != null) displayList.add(url);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Proceso de subida
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

            loadExistingPhotos();
            return;
        }

        Uri currentUri = selectedImages.get(currentUploadIndex);
        File originalFile = FileUtils.getFileFromUri(this, currentUri);

        // Notificamos progreso
        String msg = "Analizando foto " + (currentUploadIndex + 1) + " de " + selectedImages.size();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        faceManager.processGroupPhoto(originalFile, new FaceManager.GroupPhotoCallback() {
            @Override
            public void onProcessed(Bitmap bitmap) {
                runOnUiThread(() -> {
                    File safeFile = saveBitmapToTempFile(bitmap);
                    if (safeFile != null) {
                        uploadToCloudinary(safeFile);
                    } else {
                        currentUploadIndex++;
                        processAndUploadNextImage();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UploadActivityPhotosActivity.this, "Error privacidad. Subiendo original...", Toast.LENGTH_SHORT).show();
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
                    processAndUploadNextImage();
                })
                .addOnFailureListener(e -> {
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
}