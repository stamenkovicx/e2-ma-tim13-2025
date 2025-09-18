package com.example.myapplication.presentation.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.FriendsAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private TextView tvNoFriends;
    private LinearLayout btnSearchUsers;
    private Button btnScanQrCode, btnCreateAlliance;

    private UserRepository userRepository;
    private String currentUserId;

    // Deklaracija ActivityResultLauncher-a
    private ActivityResultLauncher<Intent> qrCodeScannerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        tvNoFriends = findViewById(R.id.tvNoFriends);
        btnSearchUsers = findViewById(R.id.btnSearchUsers);
        btnScanQrCode = findViewById(R.id.btnScanQrCode);
        btnCreateAlliance = findViewById(R.id.btnCreateAlliance);

        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsAdapter = new FriendsAdapter(new ArrayList<>());
        friendsRecyclerView.setAdapter(friendsAdapter);

        // Inicijalizacija ActivityResultLauncher-a
        qrCodeScannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String scannedUserId = data.getStringExtra("scannedUserId");
                            if (scannedUserId != null && !scannedUserId.isEmpty()) {
                                userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
                                    @Override
                                    public void onSuccess(User currentUser) {
                                        if (currentUser != null) {
                                            handleFriendRequestLogic(scannedUserId, currentUser);
                                        } else {
                                            Toast.makeText(FriendsActivity.this, "Error loading user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(FriendsActivity.this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(FriendsActivity.this, "Scanning failed or was canceled.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnSearchUsers.setOnClickListener(v-> {
            Intent intent = new Intent(FriendsActivity.this, SearchUsersActivity.class);
            startActivity(intent);
        });

        btnScanQrCode.setOnClickListener(v -> {
            // Pokretanje vaše QRScannerActivity
            Intent intent = new Intent(FriendsActivity.this, QRScannerActivity.class);
            qrCodeScannerLauncher.launch(intent);
        });

        btnCreateAlliance.setOnClickListener(v -> {
            showCreateAllianceDialog();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFriends();
    }

    private void loadFriends() {
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User currentUser) {
                if (currentUser != null && currentUser.getFriends() != null && !currentUser.getFriends().isEmpty()) {
                    tvNoFriends.setVisibility(View.GONE);
                    friendsRecyclerView.setVisibility(View.VISIBLE);

                    userRepository.getUsersByIds(currentUser.getFriends(), new UserRepository.OnCompleteListener<List<User>>() {
                        @Override
                        public void onSuccess(List<User> friends) {
                            friendsAdapter.setFriends(friends);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(FriendsActivity.this, "Error loading friends.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    tvNoFriends.setVisibility(View.VISIBLE);
                    friendsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FriendsActivity.this, "Error loading user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFriendRequestLogic(String scannedUserId, User currentUser) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Provjera da li dodajete samog sebe
        if (scannedUserId.equals(currentUserId)) {
            Toast.makeText(this, "You cannot add yourself as a friend.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Provjera da li ste vec prijatelji
        if (currentUser.getFriends() != null && currentUser.getFriends().contains(scannedUserId)) {
            Toast.makeText(this, "You are already friends with this user.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Provejra da li ste vec poslali zahtev
        if (currentUser.getFriendRequestsSent() != null && currentUser.getFriendRequestsSent().contains(scannedUserId)) {
            Toast.makeText(this, "Friend request already sent.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Provjera da li vam je taj korisnik vec poslao zahtev
        if (currentUser.getFriendRequestsReceived() != null && currentUser.getFriendRequestsReceived().contains(scannedUserId)) {
            Toast.makeText(this, "This user has already sent you a friend request. Accept it from your requests list.", Toast.LENGTH_LONG).show();
            return;
        }

        // Slanje zahtjeva, jer su sve provjere prosle
        userRepository.sendFriendRequest(currentUserId, scannedUserId, new UserRepository.OnCompleteListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FriendsActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FriendsActivity.this, "Error sending friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateAllianceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_alliance, null);
        builder.setView(dialogView);

        final EditText etAllianceName = dialogView.findViewById(R.id.etAllianceName);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        final AlertDialog dialog = builder.create();

        btnCreate.setOnClickListener(v -> {
            String allianceName = etAllianceName.getText().toString().trim();
            if (allianceName.isEmpty()) {
                Toast.makeText(FriendsActivity.this, "Alliance name cannot be empty.", Toast.LENGTH_SHORT).show();
            } else {
                createAlliance(allianceName);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void createAlliance(String allianceName) {
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    if (user.getAllianceId() != null && !user.getAllianceId().isEmpty()) {
                        Toast.makeText(FriendsActivity.this, "You are already in an alliance.", Toast.LENGTH_SHORT).show();
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FriendsActivity.this, "Error creating alliance.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}