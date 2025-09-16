package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileAvatar;
    private TextView usernameTextView;
    private TextView titleTextView;
    private TextView levelTextView;
    private TextView xpTextView;
    private Button addFriendButton;
    private TextView badgesTitleTextView;
    private TextView equipmentTitleTextView;

    private UserRepository userRepository;
    private String viewedUserId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Inicijalizacija UI komponenti
        profileAvatar = findViewById(R.id.imageViewProfileAvatar);
        usernameTextView = findViewById(R.id.textViewProfileUsername);
        titleTextView = findViewById(R.id.textViewProfileTitle);
        levelTextView = findViewById(R.id.textViewProfileLevel);
        xpTextView = findViewById(R.id.textViewProfileXp);
        addFriendButton = findViewById(R.id.buttonAddFriend);
        badgesTitleTextView = findViewById(R.id.textViewBadgesTitle);
        equipmentTitleTextView = findViewById(R.id.textViewEquipmentTitle);

        // Sakrivamo QR kod komponente, jer je ovo profil drugog korisnika
        findViewById(R.id.imageViewQrCode).setVisibility(View.GONE);
        findViewById(R.id.textViewQrCodeTitle).setVisibility(View.GONE);

        // Inicijalizacija repozitorijuma i ID-jeva
        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewedUserId = getIntent().getStringExtra("USER_ID");

        if (viewedUserId != null) {
            loadUserProfile(viewedUserId);
        } else {
            Toast.makeText(this, "User ID not provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserProfile(String userId) {
        userRepository.getUserById(userId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    displayProfileData(user);
                    setupAddFriendButton(user);
                } else {
                    Toast.makeText(UserProfileActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Failed to load user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProfileData(User user) {
        usernameTextView.setText(user.getUsername());
        titleTextView.setText(user.getTitle());
        levelTextView.setText(getString(R.string.level_label, user.getLevel()));
        // Prikaz XP-a
        int requiredXp = 0; // TODO: Dohvatiti potrebni XP iz baze ili pomoćne klase
        xpTextView.setText("XP: " + user.getXp() + " / " + requiredXp);

        // TODO: Ažurirati UI za bedževe i opremu (RecyclerView adapters)
        badgesTitleTextView.setVisibility(View.VISIBLE);
        equipmentTitleTextView.setVisibility(View.VISIBLE);
    }

    private void setupAddFriendButton(User user) {
        // Sakrij dugme ako je korisnik već prijatelj
        if (user.getFriends() != null && user.getFriends().contains(currentUserId)) {
            addFriendButton.setText(R.string.already_friends);
            addFriendButton.setEnabled(false);
            return;
        }

        // Sakrij dugme ako je zahtev već poslat
        if (user.getFriendRequestsReceived() != null && user.getFriendRequestsReceived().contains(currentUserId)) {
            addFriendButton.setText(R.string.request_sent);
            addFriendButton.setEnabled(false);
            return;
        }

        addFriendButton.setVisibility(View.VISIBLE);
        addFriendButton.setOnClickListener(v -> {
            userRepository.sendFriendRequest(currentUserId, viewedUserId, new UserRepository.OnCompleteListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(UserProfileActivity.this, R.string.friend_request_sent, Toast.LENGTH_SHORT).show();
                    addFriendButton.setText(R.string.request_sent);
                    addFriendButton.setEnabled(false);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(UserProfileActivity.this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}