package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.R;
import com.onesignal.OneSignal;

public class HomeActivity extends AppCompatActivity {

    private Button btnLogout, btnProfile, btnGoToCreateTask, btnGoToTaskViewer, btnManageCategories,
            btnViewStatistics, btnViewFriends,btnBossFight;
    private FirebaseAuth mAuth;
    private LinearLayout notificationContainer;
    private UserRepository userRepository;
    private String currentUserId;
    private TextView tvAllianceInvitationNotification;
    private RelativeLayout notificationsButtonContainer;
    private ImageButton btnNotifications;
    private TextView tvUnreadNotificationsCount;
    private TaskRepository taskRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!OneSignal.userProvidedPrivacyConsent()) {
            // Odloži dijalog dok UI nije spreman
            new android.os.Handler().post(() -> showPrivacyConsentDialog());
        }

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = mAuth.getCurrentUser().getUid();
        taskRepository = new TaskRepositoryFirebaseImpl();
        checkExpiredTasks();

        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        btnGoToCreateTask = findViewById(R.id.btnGoToCreateTask);
        btnGoToTaskViewer = findViewById(R.id.btnGoToTaskViewer);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);
        btnViewFriends = findViewById(R.id.btnViewFriends);
        notificationContainer = findViewById(R.id.notificationContainer);
        tvAllianceInvitationNotification = findViewById(R.id.tvAllianceInvitationNotification);
        notificationsButtonContainer = findViewById(R.id.notificationsButtonContainer);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvUnreadNotificationsCount = findViewById(R.id.tvUnreadNotificationsCount);
        btnBossFight = findViewById(R.id.bossButton);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut(); /* Odjava korisnika s Firebase-a */

            OneSignal.removeExternalUserId();
            OneSignal.deleteTag("user_id");

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
        notificationContainer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AllianceInvitationsActivity.class);
            startActivity(intent);
        });
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
        btnBossFight.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BossFightActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        checkAndLoadAllianceInvitations();
        updateUnreadNotificationsCount();
    }

    private void updateUnreadNotificationsCount() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRepository.getUnreadNotificationsCount(currentUserId, new UserRepository.OnCompleteListener<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (count > 0) {
                    tvUnreadNotificationsCount.setText(String.valueOf(count));
                    tvUnreadNotificationsCount.setVisibility(View.VISIBLE);
                } else {
                    tvUnreadNotificationsCount.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Exception e) {
                // Ako dođe do greške, sakrij brojač
                tvUnreadNotificationsCount.setVisibility(View.GONE);
            }
        });
    }

    private void checkAndLoadAllianceInvitations() {
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getAllianceInvitationsReceived() != null && !user.getAllianceInvitationsReceived().isEmpty()) {
                    notificationContainer.setVisibility(View.VISIBLE);
                } else {
                    notificationContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Skrijt notifikaciju u slučaju greške
                notificationContainer.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error checking for alliance invitations.", Toast.LENGTH_SHORT).show();
            }
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
    private void checkExpiredTasks() {
        if (currentUserId == null || currentUserId.isEmpty()) return;

        Log.d("HomeActivity", "Pokrećem proveru isteklih zadataka...");
        taskRepository.checkAndDeactivateExpiredTasks(currentUserId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                Log.d("HomeActivity", "Provera isteklih zadataka završena uspešno.");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("HomeActivity", "Greška pri proveri isteklih zadataka.", e);
            }
        });
    }
}