package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ParentActivity extends AppCompatActivity {


    private Button btnRegisterChild, btnViewPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        btnRegisterChild = findViewById(R.id.btnRegisterChild);
        btnViewPhotos = findViewById(R.id.btnViewPhotos);

        // Botón para registrar un hijo
        btnRegisterChild.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActivity.this, RegisterChildActivity.class);
            startActivity(intent);
            finish();
        });

        // Botón para ver fotos subidas por profesores
        btnViewPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActivity.this, GalleryActivity.class);
            startActivity(intent);
            finish();
        });
    }


}
