package com.example.myapplication.presentation.ui.registration;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.DatabaseHelper;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ImageView avatar1, avatar2, avatar3, avatar4, avatar5;
    private ImageView currentSelectedAvatarView;
    private String selectedAvatar = "";
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(this);

        // Povezivanje elemenata iz XML-a
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Povezivanje avatar ImageView elemenata
        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);

        // Postavljanje listenera za odabir avatara
        avatar1.setOnClickListener(v -> handleAvatarSelection(avatar1, "avatar1"));
        avatar2.setOnClickListener(v -> handleAvatarSelection(avatar2, "avatar2"));
        avatar3.setOnClickListener(v -> handleAvatarSelection(avatar3, "avatar3"));
        avatar4.setOnClickListener(v -> handleAvatarSelection(avatar4, "avatar4"));
        avatar5.setOnClickListener(v -> handleAvatarSelection(avatar5, "avatar5"));

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields must be filled out.", Toast.LENGTH_SHORT).show();
            } else if (selectedAvatar.isEmpty()) {
                Toast.makeText(this, "Please select an avatar.", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            } else {
                // Registracija korisnika na Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Slanje verifikacionog emaila
                                    firebaseUser.sendEmailVerification()
                                            .addOnCompleteListener(emailTask -> {
                                                if (emailTask.isSuccessful()) {
                                                    // Ako je email uspjesno poslat -> cuvanje korisnika u lokalnu bazu
                                                    if (!databaseHelper.checkUser(email)) {
                                                        User newUser = new User(username, email, password, selectedAvatar);
                                                        boolean isAdded = databaseHelper.addUser(newUser);

                                                        if (isAdded) {
                                                            Toast.makeText(RegistrationActivity.this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();
                                                            // logika za prelazak na drugu stranicu
                                                        } else {
                                                            Toast.makeText(RegistrationActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                } else {
                                                    // Slanje emaila nije uspjelo
                                                    Toast.makeText(RegistrationActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                // Registracija na Firebase-u nije uspjela
                                Toast.makeText(RegistrationActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void handleAvatarSelection(ImageView avatarView, String avatarName) {
        if (currentSelectedAvatarView != null) {
            currentSelectedAvatarView.setBackground(null);
        }

        avatarView.setBackgroundResource(R.drawable.selected_avatar_border);

        selectedAvatar = avatarName;
        currentSelectedAvatarView = avatarView;
    }
}