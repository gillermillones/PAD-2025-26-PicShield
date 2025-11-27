package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ParentActivity extends AppCompatActivity {

    private Button btnRegisterChild, btnViewPhotos, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        btnRegisterChild = findViewById(R.id.btnRegisterChild);
        btnViewPhotos = findViewById(R.id.btnViewPhotos);
        btnLogout = findViewById(R.id.btnLogout); // Botón de logout

        // Botón para registrar un hijo
        btnRegisterChild.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActivity.this, RegisterChildActivity.class);
            startActivity(intent);
            //finish();
        });

        // Botón para ver fotos subidas por profesores
        btnViewPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActivity.this, GalleryActivity.class);
            startActivity(intent);
            //finish();
        });

        // Botón para cerrar sesión
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Cerrar sesión
            Intent intent = new Intent(ParentActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
