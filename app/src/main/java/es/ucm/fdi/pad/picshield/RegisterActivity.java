package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {


    private EditText etEmail, etPassword;
    private Button btnRegister, btnBack;
    private RadioGroup rgUserType;
    private RadioButton rbParent, rbTeacher;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
        rgUserType = findViewById(R.id.rgUserType);
        rbParent = findViewById(R.id.rbParent);
        rbTeacher = findViewById(R.id.rbTeacher);

        btnBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String userType = rbParent.isChecked() ? "Padre" : "Profesor";

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if(password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear usuario en Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) {
                                // Guardar userType en Firestore
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("email", email);
                                userMap.put("userType", userType);

                                db.collection("users").document(user.getUid())
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "Registrado como " + userType, Toast.LENGTH_SHORT).show();

                                            // Redirigir según tipo de usuario
                                            if("Padre".equals(userType)) {
                                                startActivity(new Intent(RegisterActivity.this, ParentActivity.class));
                                            } else {
                                                //startActivity(new Intent(RegisterActivity.this, TeacherActivity.class));
                                            }
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Error guardando el tipo de usuario", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


}
