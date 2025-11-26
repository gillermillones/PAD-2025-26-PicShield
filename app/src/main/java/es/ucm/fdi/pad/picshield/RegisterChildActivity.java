package es.ucm.fdi.pad.picshield;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterChildActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etDNI;
    private ImageView ivFacePhoto;
    private CheckBox cbAllowPhotos;
    private Button btnSaveChild;
    private RecyclerView rvChildren;

    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Child> childrenList = new ArrayList<>();
    private ChildAdapter adapter;

    private ActivityResultLauncher<String> pickImageLauncher;

    // Configura aquí tu Cloudinary
    private final String CLOUD_NAME = "TU_CLOUD_NAME";
    private final String UPLOAD_PRESET = "TU_UPLOAD_PRESET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap config = new HashMap();
        config.put("cloud_name", "TU_CLOUD_NAME");
        config.put("api_key", "TU_API_KEY");  // opcional para unsigned uploads
        config.put("api_secret", "TU_API_SECRET"); // opcional para unsigned uploads
        MediaManager.init(this, config);
        setContentView(R.layout.activity_register_child);

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

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null){
                        selectedImageUri = uri;
                        ivFacePhoto.setImageURI(uri);
                    }
                });

        ivFacePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSaveChild.setOnClickListener(v -> saveChild());

        loadChildren();
    }

    private void saveChild() {
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

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) return;

        Map<String,Object> params = new HashMap<>();
        params.put("upload_preset", UPLOAD_PRESET);
        params.put("public_id", user.getUid() + "_" + dni);

        MediaManager.get().upload(selectedImageUri)
                .unsigned(UPLOAD_PRESET)
                .options(params)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String photoUrl = resultData.get("secure_url").toString();

                        Child child = new Child(firstName,lastName,dni,photoUrl,allowPhotos);
                        db.collection("users")
                                .document(user.getUid())
                                .collection("children")
                                .document(dni)
                                .set(child)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterChildActivity.this,"Hijo registrado",Toast.LENGTH_SHORT).show();
                                    clearFields();
                                    loadChildren();
                                })
                                .addOnFailureListener(e -> Toast.makeText(RegisterChildActivity.this,"Error al guardar hijo",Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(RegisterChildActivity.this,"Error al subir foto: " + error.getDescription(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                });
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

        db.collection("users")
                .document(user.getUid())
                .collection("children")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();
                    for(DocumentSnapshot doc : queryDocumentSnapshots){
                        Child child = doc.toObject(Child.class);
                        childrenList.add(child);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
