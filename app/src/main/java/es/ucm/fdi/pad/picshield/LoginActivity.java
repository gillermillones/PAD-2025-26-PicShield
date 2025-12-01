package es.ucm.fdi.pad.picshield;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if(cm.getActiveNetworkInfo() == null){
                Toast.makeText(LoginActivity.this, "No tienes conexion a internet", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user == null) {
                                Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
                                return;
                            }
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

                                            int type = dbUserType.intValue(); // 1 = padre, 0 = profesor

                                            if(type == 1) {
                                                // PADRE
                                                startActivity(new Intent(LoginActivity.this, ParentActivity.class));
                                                finish();
                                            }
                                            else if(type == 0) {
                                                // PROFESOR
                                                startActivity(new Intent(LoginActivity.this, TeacherActivity.class));
                                                finish();
                                            }
                                            else {
                                                Toast.makeText(LoginActivity.this, "Tipo de usuario desconocido", Toast.LENGTH_SHORT).show();
                                            }

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Documento de usuario no existe", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(LoginActivity.this, "Error al obtener tipo de usuario", Toast.LENGTH_SHORT).show()
                                    );


                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    public boolean InternetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
}
