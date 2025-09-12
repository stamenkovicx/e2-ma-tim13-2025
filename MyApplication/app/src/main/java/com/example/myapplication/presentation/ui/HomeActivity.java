package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.myapplication.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.R;

public class HomeActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnProfile;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.btnLogout);

        btnProfile = findViewById(R.id.btnProfile);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); /* Odjava korisnika s Firebase-a */

            // Prelazak na ekran za prijavu i zatvaranje HomeActivity
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}