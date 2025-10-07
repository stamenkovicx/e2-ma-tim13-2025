package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.data.database.LevelingSystemHelper;
import com.example.myapplication.data.database.TaskRepositoryFirebaseImpl;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.TaskRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.R;
import com.onesignal.OneSignal;

public class HomeActivity extends AppCompatActivity {

    private TextView tvUsername, tvLevel, tvTitle, tvXP;
    private ProgressBar xpProgressBar;
    private CardView notificationContainer;
    private ImageButton btnProfile, btnNotifications;
    private TextView tvUnreadNotificationsCount;
    private TextView tvAllianceInvitationNotification;

    private LinearLayout btnGoToCreateTask, btnGoToTaskViewer, btnViewStatistics,
            btnBossFight, btnViewFriends, btnManageCategories;

    private Button btnLogout;

    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private String currentUserId;
    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!OneSignal.userProvidedPrivacyConsent()) {
            new android.os.Handler().post(() -> showPrivacyConsentDialog());
        }

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = mAuth.getCurrentUser().getUid();
        taskRepository = new TaskRepositoryFirebaseImpl();

        initializeViews();
        setupClickListeners();
        loadUserData();
        checkExpiredTasks();
    }

    private void initializeViews() {
        // Header elementi
        tvUsername = findViewById(R.id.tvUsername);
        tvLevel = findViewById(R.id.tvLevel);
        tvTitle = findViewById(R.id.tvTitle);
        tvXP = findViewById(R.id.tvXP);
        xpProgressBar = findViewById(R.id.xpProgressBar);

        // Notifikacije
        notificationContainer = findViewById(R.id.notificationContainer);
        tvAllianceInvitationNotification = findViewById(R.id.tvAllianceInvitationNotification);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvUnreadNotificationsCount = findViewById(R.id.tvUnreadNotificationsCount);
        btnProfile = findViewById(R.id.btnProfile);

        // Grid akcije
        btnGoToCreateTask = findViewById(R.id.btnGoToCreateTask);
        btnGoToTaskViewer = findViewById(R.id.btnGoToTaskViewer);
        btnViewStatistics = findViewById(R.id.btnViewStatistics);
        btnBossFight = findViewById(R.id.bossButton);
        btnViewFriends = findViewById(R.id.btnViewFriends);
        btnManageCategories = findViewById(R.id.btnManageCategories);

        // Logout
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logoutUser());

        // Header dugmad
        btnProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        btnNotifications.setOnClickListener(v -> navigateTo(NotificationsActivity.class));

        // Grid akcije
        btnGoToCreateTask.setOnClickListener(v -> navigateTo(CreateTaskActivity.class));
        btnGoToTaskViewer.setOnClickListener(v -> navigateTo(TaskViewerActivity.class));
        btnManageCategories.setOnClickListener(v -> navigateTo(CategoriesActivity.class));
        btnViewStatistics.setOnClickListener(v -> navigateTo(StatisticsActivity.class));
        btnViewFriends.setOnClickListener(v -> navigateTo(FriendsActivity.class));
        btnBossFight.setOnClickListener(v -> navigateTo(BossFightActivity.class));

        // Alliance notifikacija
        notificationContainer.setOnClickListener(v -> navigateTo(AllianceInvitationsActivity.class));
    }

    private void loadUserData() {
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    // Postavi podatke korisnika
                    tvUsername.setText(user.getUsername());
                    tvLevel.setText(String.valueOf(user.getLevel()));
                    tvTitle.setText(user.getTitle());
                    tvXP.setText(String.valueOf(user.getXp()));

                    updateXpProgressBar(user);

                    checkAndLoadAllianceInvitations(user);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateXpProgressBar(User user) {
        int currentLevelXp = user.getXp();
        int xpForNextLevel = LevelingSystemHelper.getRequiredXpForNextLevel(user.getLevel());

        int progress = (int) ((currentLevelXp / (float) xpForNextLevel) * 100);
        xpProgressBar.setProgress(progress);
    }

    private void checkAndLoadAllianceInvitations(User user) {
        if (user != null && user.getAllianceInvitationsReceived() != null &&
                !user.getAllianceInvitationsReceived().isEmpty()) {
            notificationContainer.setVisibility(View.VISIBLE);
            tvAllianceInvitationNotification.setText(
                    "You have " + user.getAllianceInvitationsReceived().size() + " new alliance invitations!"
            );
        } else {
            notificationContainer.setVisibility(View.GONE);
        }
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(HomeActivity.this, destination);
        startActivity(intent);
    }

    private void logoutUser() {
        mAuth.signOut();
        OneSignal.removeExternalUserId();
        OneSignal.deleteTag("user_id");

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        updateUnreadNotificationsCount();
    }

    private void updateUnreadNotificationsCount() {
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
                tvUnreadNotificationsCount.setVisibility(View.GONE);
            }
        });
    }

    private void showPrivacyConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification permission")
                .setMessage("Our application uses push notifications to inform about news. Do you allow receiving notifications?")
                .setCancelable(false)
                .setPositiveButton("Accept", (dialog, which) -> OneSignal.provideUserConsent(true))
                .setNegativeButton("No thanks", (dialog, which) -> OneSignal.provideUserConsent(false))
                .show();
    }

    private void checkExpiredTasks() {
        if (currentUserId == null || currentUserId.isEmpty()) return;

        Log.d("HomeActivity", "Checking expired tasks...");
        taskRepository.checkAndDeactivateExpiredTasks(currentUserId, new TaskRepository.OnTaskUpdatedListener() {
            @Override
            public void onSuccess() {
                Log.d("HomeActivity", "Expired tasks check completed successfully.");
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("HomeActivity", "Error checking expired tasks.", e);
            }
        });
    }
}