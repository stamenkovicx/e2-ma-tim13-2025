package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.myapplication.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.R;

public class HomeActivity extends AppCompatActivity {

    private Button btnLogout, btnProfile, btnGoToCreateTask, btnGoToTaskViewer, btnManageCategories,
            btnViewStatistics, btnViewFriends;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        btnGoToCreateTask = findViewById(R.id.btnGoToCreateTask);
        btnGoToTaskViewer = findViewById(R.id.btnGoToTaskViewer);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);
        btnViewFriends = findViewById(R.id.btnViewFriends);

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

        btnGoToCreateTask.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateTaskActivity.class);
            startActivity(intent);
        });

        btnGoToTaskViewer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TaskViewerActivity.class);
            startActivity(intent);
        });
        btnManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CategoriesActivity.class);
            startActivity(intent);
        });
        btnViewStatistics.setOnClickListener(v-> {
            Intent intent = new Intent(HomeActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
        btnViewFriends.setOnClickListener(v-> {
            Intent intent = new Intent(HomeActivity.this, FriendsActivity.class);
            startActivity(intent);
        });
    }
}