package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private RadioGroup rgUserType;
    private RadioButton rbParent, rbTeacher;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        rgUserType = findViewById(R.id.rgUserType);
        rbParent = findViewById(R.id.rbParent);
        rbTeacher = findViewById(R.id.rbTeacher);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if(documentSnapshot.exists()) {
                                            Long dbUserType = documentSnapshot.getLong("userType");
                                            if(dbUserType == null) {
                                                Toast.makeText(LoginActivity.this, "Tipo de usuario no definido", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if(dbUserType == 1) { // Padre
                                                startActivity(new Intent(LoginActivity.this, ParentActivity.class));
                                                finish();
                                            } else if(dbUserType == 0) { // Profesor
                                               // startActivity(new Intent(LoginActivity.this, TeacherActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Tipo de usuario desconocido", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Documento de usuario no existe", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error al obtener tipo de usuario", Toast.LENGTH_SHORT).show());

                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
