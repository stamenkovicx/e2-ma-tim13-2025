package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.presentation.ui.HomeActivity;
import com.example.myapplication.presentation.ui.RegistrationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegisterLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        // PROVJERA STATUSA KORISNIKA
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Povezivanje elemenata iz XML-a
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterLink = findViewById(R.id.btnRegisterLink);

        // Postavljanje listenera za dugme za prijavu
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase logika za prijavu
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                updateFcmTokenAndProceed();
                            } else {
                                Toast.makeText(MainActivity.this, "Please verify your email to log in.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // listener za dugme za registraciju
        btnRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void updateFcmTokenAndProceed() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        // Dohvati FCM token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String fcmToken = task.getResult();
                FirebaseFirestore.getInstance().collection("users")
                        .document(user.getUid())
                        .update("fcmToken", fcmToken)
                        .addOnSuccessListener(aVoid -> {
                            // Token uspjesno sacuvan, pokreni HomeActivity
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Greska pri cuvanju tokena, ali svejedno pokreni HomeActivity
                            Toast.makeText(MainActivity.this, "Error saving token: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        });
            } else {
                // Greska pri dobijanju tokena, pokreni HomeActivity
                Toast.makeText(MainActivity.this, "Failed to get FCM token.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}