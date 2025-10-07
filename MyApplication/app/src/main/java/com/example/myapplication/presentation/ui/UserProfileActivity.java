package com.example.myapplication.presentation.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.UserEquipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileAvatar;
    private TextView usernameTextView;
    private TextView levelTextView;
    private TextView xpTextView;
    private Button addFriendButton;
    private TextView badgesTitleTextView;
    private TextView equipmentTitleTextView;

    private UserRepository userRepository;
    private String viewedUserId;
    private String currentUserId;
    private User currentUser;
    private LinearLayout llEquipmentContainer;
    private ImageView ivQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Inicijalizacija UI komponenti
        profileAvatar = findViewById(R.id.profileAvatar);
        usernameTextView = findViewById(R.id.textViewProfileUsername);
        levelTextView = findViewById(R.id.textViewProfileLevel);
        xpTextView = findViewById(R.id.textViewProfileXp);
        addFriendButton = findViewById(R.id.buttonAddFriend);
        badgesTitleTextView = findViewById(R.id.textViewBadgesTitle);
        equipmentTitleTextView = findViewById(R.id.textViewEquipmentTitle);
        llEquipmentContainer = findViewById(R.id.llEquipmentContainer);
        ivQRCode = findViewById(R.id.ivQRCode);

        // Inicijalizacija repozitorijuma i ID-jeva
        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewedUserId = getIntent().getStringExtra("userId");

        if (viewedUserId != null) {
            loadAllUserData();
        } else {
            Toast.makeText(this, "User ID not provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAllUserData() {
        // Korak 1: Dohvati podatke o trenutnom korisniku
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;
                    // Korak 2: Nakon sto smo dohvatili trenutnog, dohvati prikazanog
                    loadUserProfile(viewedUserId);
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to load current user data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserProfileActivity.this, "Failed to load current user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        String usernameAndTitle = user.getUsername() + " (" + user.getTitle() + ")";
        usernameTextView.setText(usernameAndTitle);
        levelTextView.setText(String.valueOf(user.getLevel()));
        int requiredXp = 0;
        xpTextView.setText(String.valueOf(user.getXp()));

        int avatarResourceId = getResources().getIdentifier(user.getAvatar(), "drawable", getPackageName());
        profileAvatar.setImageResource(avatarResourceId);

        llEquipmentContainer.removeAllViews();
        for (UserEquipment item : user.getEquipment()) {
            Equipment equipment = ItemRepository.getEquipmentById(item.getEquipmentId());
            if (equipment != null) {
                ImageView equipmentView = new ImageView(this);
                int resourceId = getResources().getIdentifier(equipment.getIconResourceId(), "drawable", getPackageName());
                if (resourceId != 0) {
                    equipmentView.setImageResource(resourceId);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                    params.setMargins(0, 0, 16, 0);
                    equipmentView.setLayoutParams(params);
                    llEquipmentContainer.addView(equipmentView);
                }
            }
        }
        badgesTitleTextView.setVisibility(View.VISIBLE);
        equipmentTitleTextView.setVisibility(View.VISIBLE);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(user.getUserId(), BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            ivQRCode.setVisibility(View.GONE);
        }
    }

    private void setupAddFriendButton(User user) {
        // Provera da li je to vaš profil
        if (viewedUserId != null && viewedUserId.equals(currentUserId)) {
            addFriendButton.setVisibility(View.GONE);
            ivQRCode.setVisibility(View.VISIBLE);
            return;
        }

        // Provera da li je korisnik vec prijatelj
        if (currentUser.getFriends() != null && currentUser.getFriends().contains(user.getUserId())) {
            addFriendButton.setText(R.string.already_friends);
            addFriendButton.setEnabled(false);
            addFriendButton.setVisibility(View.VISIBLE);
            return;
        }

        // Provera da li je trenutnom korisniku poslat zahtev (VI PRIHVATATE)
        if (currentUser.getFriendRequestsReceived() != null && currentUser.getFriendRequestsReceived().contains(user.getUserId())) {
            addFriendButton.setText(R.string.accept_friend);
            addFriendButton.setEnabled(true);
            addFriendButton.setVisibility(View.VISIBLE);
            addFriendButton.setOnClickListener(v -> {
                userRepository.acceptFriendRequest(currentUserId, viewedUserId, new UserRepository.OnCompleteListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfileActivity.this, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                        addFriendButton.setText(R.string.already_friends);
                        addFriendButton.setEnabled(false);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Failed to accept request.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            return;
        }

        // Provera da li je zahtev vec poslat (VI ČEKATE)
        if (currentUser.getFriendRequestsSent() != null && currentUser.getFriendRequestsSent().contains(user.getUserId())) {
            addFriendButton.setText(R.string.request_sent);
            addFriendButton.setEnabled(false);
            addFriendButton.setVisibility(View.VISIBLE);
            return;
        }

        // Nijedan uslov nije ispunjen, prikažite dugme za dodavanje i dugme za skeniranje
        addFriendButton.setText(R.string.add_friend_button);
        addFriendButton.setEnabled(true);
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