package es.ucm.fdi.pad.picshield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;



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

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String userType = rbParent.isChecked() ? "Padre" : "Profesor";

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Aquí iría la validación con base de datos / Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if(documentSnapshot.exists()) {
                                           String dbUserType =  documentSnapshot.getString("userType");

                                            if("Padre".equals(dbUserType)) {
                                                startActivity(new Intent(LoginActivity.this, ParentActivity.class));
                                            } else if("Profesor".equals(dbUserType)) {
                                                //startActivity(new Intent(LoginActivity.this, TeacherActivity.class));
                                            }
                                            finish(); // Evita que vuelvan a login al pulsar "atrás"
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginActivity.this, "Error al obtener tipo de usuario", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            Toast.makeText(LoginActivity.this, "Login como " + userType, Toast.LENGTH_SHORT).show();
        });

        // Register
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
