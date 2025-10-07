package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.presentation.ui.HomeActivity;
import com.example.myapplication.presentation.ui.RegistrationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onesignal.OneSignal;
import org.json.JSONObject;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegisterLink;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //OneSignal.provideUserConsent(false);
        if (!OneSignal.userProvidedPrivacyConsent()) {
            // Odloži dijalog dok UI nije spreman
            new android.os.Handler().post(() -> showPrivacyConsentDialog());
        }

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepositoryFirebaseImpl();

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

                                String currentUserId = user.getUid();
                                OneSignal.setExternalUserId(currentUserId);

                                //logika za dodavanje tagova za OneSignal
                                userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<com.example.myapplication.domain.models.User>() {
                                    @Override
                                    public void onSuccess(com.example.myapplication.domain.models.User fetchedUser) {
                                        if (fetchedUser != null && fetchedUser.getAllianceId() != null) {
                                            try {
                                                JSONObject tags = new JSONObject();
                                                tags.put("alliance_id", fetchedUser.getAllianceId());
                                                tags.put("user_id", currentUserId);
                                                OneSignal.sendTags(tags);
                                                Log.d("OneSignal", "Tags set after login: " + tags.toString());
                                            } catch (Exception e) {
                                                Log.e("OneSignal", "Failed to set tags after login.", e);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("MainActivity", "Failed to fetch user for tags.", e);
                                    }
                                });

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
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

    private void showPrivacyConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification permission")
                .setMessage("Our application uses push notifications to inform about news. Do you allow receiving notifications ?")
                .setCancelable(false)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Korisnik je dao pristanak
                        OneSignal.provideUserConsent(true);
                    }
                })
                .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Korisnik nije dao pristanak
                        OneSignal.provideUserConsent(false);
                    }
                })
                .show();
    }
}