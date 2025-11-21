package es.ucm.fdi.pad.picshield;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterChildActivity extends AppCompatActivity {


    private EditText etFirstName, etLastName, etDNI;
    private ImageView ivFacePhoto;
    private CheckBox cbAllowPhotos;
    private Button btnSaveChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_child);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDNI = findViewById(R.id.etDNI);
        ivFacePhoto = findViewById(R.id.ivFacePhoto);
        cbAllowPhotos = findViewById(R.id.cbAllowPhotos);
        btnSaveChild = findViewById(R.id.btnSaveChild);

        // TODO: Agregar funcionalidad para seleccionar foto y guardar datos en Firebase
        btnSaveChild.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String dni = etDNI.getText().toString().trim();
            boolean allowPhotos = cbAllowPhotos.isChecked();

            if(firstName.isEmpty() || lastName.isEmpty() || dni.isEmpty()) {
                Toast.makeText(RegisterChildActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(RegisterChildActivity.this, "Hijo registrado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


}
