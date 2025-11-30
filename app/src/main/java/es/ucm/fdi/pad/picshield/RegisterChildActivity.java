package es.ucm.fdi.pad.picshield;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class RegisterChildActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etDNI;
    private ImageView ivFacePhoto;
    private CheckBox cbAllowPhotos;
    private Button btnSaveChild, buttonBack;
    private RecyclerView rvChildren;

    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;
    private ActivityResultLauncher<String> pickImageLauncher;

    // --- FACE++ ---
    private FaceManager faceManager;

    // --- CLOUDINARY ---
    private final String CLOUD_NAME = "degsfj3pv";
    private final String UPLOAD_PRESET = "childphoto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child);

        // Inicializar FaceManager
        faceManager = new FaceManager(this, new FaceApiClient());

        buttonBack = findViewById(R.id.btnBack);
        buttonBack.setOnClickListener(v -> finish());

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDNI = findViewById(R.id.etDNI);
        ivFacePhoto = findViewById(R.id.ivFacePhoto);
        cbAllowPhotos = findViewById(R.id.cbAllowPhotos);
        btnSaveChild = findViewById(R.id.btnSaveChild);
        rvChildren = findViewById(R.id.rvChildren);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new ChildAdapter(childrenList, this);
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        rvChildren.setAdapter(adapter);

        // Updated Launcher with MIME Type Check
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String mimeType = getContentResolver().getType(uri);
                        if (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg"))) {
                            selectedImageUri = uri;
                            ivFacePhoto.setImageURI(uri);
                        } else {
                            Toast.makeText(this, "Error: La imagen debe ser JPG", Toast.LENGTH_LONG).show();
                            selectedImageUri = null;
                            ivFacePhoto.setImageResource(android.R.drawable.ic_menu_camera);
                        }
                    }
                });

        ivFacePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSaveChild.setOnClickListener(v -> saveChildProcess());
        loadChildren();
    }

    private void saveChildProcess() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dni = etDNI.getText().toString().trim();
        boolean allowPhotos = cbAllowPhotos.isChecked();

        if(firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty()){
            Toast.makeText(this,"Completa todos los campos",Toast.LENGTH_SHORT).show();
            return;
        }
        if(selectedImageUri == null){
            Toast.makeText(this,"Selecciona una foto del niño",Toast.LENGTH_SHORT).show();
            return;
        }

        // Bloquear botón
        btnSaveChild.setEnabled(false);
        btnSaveChild.setText("Procesando...");

        // 1. Si NO permite fotos, primero añadimos a Face++
        if (!allowPhotos) {
            Toast.makeText(this, "Registrando en privacidad (Face++)...", Toast.LENGTH_SHORT).show();

            File imageFile = FileUtils.getFileFromUri(this, selectedImageUri);
            if(imageFile != null) {
                faceManager.addIndividualPhoto(imageFile, new FaceManager.ProcessCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterChildActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Una vez añadido a FaceSet, subimos a Cloudinary y Firebase
                            uploadImageToCloudinary(selectedImageUri, firstName, lastName, dni, allowPhotos);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterChildActivity.this, "Error privacidad: " + error, Toast.LENGTH_LONG).show();
                            btnSaveChild.setEnabled(true);
                            btnSaveChild.setText("Guardar Hijo");
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Error leyendo archivo de imagen", Toast.LENGTH_SHORT).show();
                btnSaveChild.setEnabled(true);
            }
        } else {
            // Si permite fotos, subimos directamente sin Face++
            uploadImageToCloudinary(selectedImageUri, firstName, lastName, dni, allowPhotos);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri, String firstName, String lastName, String dni, boolean allowPhotos) {
        new Thread(() -> {
            try {
                String boundary = "-----" + UUID.randomUUID().toString();
                URL url = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream output = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

                writer.write("--" + boundary + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                writer.write(UPLOAD_PRESET + "\r\n");

                writer.write("--" + boundary + "\r\n");
                writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n");
                writer.write("Content-Type: image/jpeg\r\n\r\n");
                writer.flush();

                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
                inputStream.close();
                writer.write("\r\n--" + boundary + "--\r\n");
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    String json = response.toString();
                    String secureUrl = extractSecureUrl(json);

                    runOnUiThread(() -> saveChildInFirestore(firstName, lastName, dni, secureUrl, allowPhotos));
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterChildActivity.this,"Error Cloudinary: " + responseCode,Toast.LENGTH_LONG).show();
                        btnSaveChild.setEnabled(true);
                        btnSaveChild.setText("Guardar Hijo");
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterChildActivity.this,"Error subida: " + e.getMessage(),Toast.LENGTH_LONG).show();
                    btnSaveChild.setEnabled(true);
                    btnSaveChild.setText("Guardar Hijo");
                });
            }
        }).start();
    }

    private void saveChildInFirestore(String firstName, String lastName, String dni, String url, boolean allowPhotos) {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) return;
        Child child = new Child(firstName, lastName, dni, url, allowPhotos);
        db.collection("users").document(user.getUid()).collection("children").document(dni)
                .set(child)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterChildActivity.this,"Hijo registrado",Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadChildren();
                    btnSaveChild.setEnabled(true);
                    btnSaveChild.setText("Guardar Hijo");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterChildActivity.this,"Error Firestore",Toast.LENGTH_SHORT).show();
                    btnSaveChild.setEnabled(true);
                });
    }

    private String extractSecureUrl(String json){
        int index = json.indexOf("\"secure_url\":\"");
        if(index == -1) return null;
        int start = index + 14;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private void clearFields(){
        etFirstName.setText("");
        etLastName.setText("");
        etDNI.setText("");
        ivFacePhoto.setImageResource(android.R.drawable.ic_menu_camera);
        cbAllowPhotos.setChecked(false);
        selectedImageUri = null;
    }

    private void loadChildren(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) return;
        db.collection("users").document(user.getUid()).collection("children").get()
                .addOnSuccessListener(query -> {
                    childrenList.clear();
                    for(DocumentSnapshot doc : query){
                        Child child = doc.toObject(Child.class);
                        childrenList.add(child);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}