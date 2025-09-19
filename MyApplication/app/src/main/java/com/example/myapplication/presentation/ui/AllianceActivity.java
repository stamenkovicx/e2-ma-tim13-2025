package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.MembersAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllianceActivity extends AppCompatActivity {

    private TextView tvAllianceName, tvAllianceLeader;
    private RecyclerView membersRecyclerView;
    private Button btnLeaveAlliance, btnDisbandAlliance, btnOpenChat; ;
    private UserRepository userRepository;
    private String currentUserId;
    private String allianceId;
    private MembersAdapter membersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance);

        tvAllianceName = findViewById(R.id.tvAllianceName);
        tvAllianceLeader = findViewById(R.id.tvAllianceLeader);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        btnLeaveAlliance = findViewById(R.id.btnLeaveAlliance);
        btnDisbandAlliance = findViewById(R.id.btnDisbandAlliance);
        btnOpenChat = findViewById(R.id.btnOpenChat);

        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        allianceId = getIntent().getStringExtra("allianceId");

        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter(new ArrayList<>());
        membersRecyclerView.setAdapter(membersAdapter);

        loadAllianceDetails();

        btnLeaveAlliance.setOnClickListener(v -> leaveAlliance());
        btnDisbandAlliance.setOnClickListener(v -> disbandAlliance());
        btnOpenChat.setOnClickListener(v -> openChat());
    }
    private void openChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("allianceId", allianceId);
        startActivity(intent);
    }
    private void loadAllianceDetails() {
        if (allianceId == null || allianceId.isEmpty()) {
            Toast.makeText(this, "Alliance not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRepository.getAllianceById(allianceId, new UserRepository.OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null) {
                    tvAllianceName.setText(alliance.getName());

                    userRepository.getUserById(alliance.getLeaderId(), new UserRepository.OnCompleteListener<User>() {
                        @Override
                        public void onSuccess(User leader) {
                            if (leader != null) {
                                tvAllianceLeader.setText("Leader: " + leader.getUsername());

                                // Logika za prikazivanje/skrivanje dugmadi
                                if (leader.getUserId().equals(currentUserId)) {
                                    btnDisbandAlliance.setVisibility(View.VISIBLE);
                                    btnLeaveAlliance.setVisibility(View.GONE);
                                } else {
                                    btnDisbandAlliance.setVisibility(View.GONE);
                                    btnLeaveAlliance.setVisibility(View.VISIBLE);
                                }
                            } else {
                                tvAllianceLeader.setText("Leader: Unknown");
                                btnDisbandAlliance.setVisibility(View.GONE);
                                btnLeaveAlliance.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            tvAllianceLeader.setText("Leader: Error");
                        }
                    });

                    userRepository.getUsersByIds(alliance.getMemberIds(), new UserRepository.OnCompleteListener<List<User>>() {
                        @Override
                        public void onSuccess(List<User> members) {
                            if (members != null) {
                                membersAdapter.setMembers(members);
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AllianceActivity.this, "Error loading members.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(AllianceActivity.this, "Alliance not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllianceActivity.this, "Failed to load alliance details.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void leaveAlliance() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Alliance")
                .setMessage("Are you sure you want to leave this alliance?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userRepository.leaveAlliance(currentUserId, allianceId, new UserRepository.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AllianceActivity.this, "You have left the alliance.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AllianceActivity.this, "Failed to leave alliance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void disbandAlliance() {
        new AlertDialog.Builder(this)
                .setTitle("Disband Alliance")
                .setMessage("Are you sure you want to disband this alliance? This action cannot be undone and all members will be removed.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userRepository.disbandAlliance(allianceId, new UserRepository.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AllianceActivity.this, "Alliance disbanded successfully.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AllianceActivity.this, "Failed to disband alliance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}