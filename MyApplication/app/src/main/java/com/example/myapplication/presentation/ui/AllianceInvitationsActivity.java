package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.AllianceInvitationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class AllianceInvitationsActivity extends AppCompatActivity {

    private RecyclerView invitationsRecyclerView;
    private AllianceInvitationsAdapter adapter;
    private TextView tvNoInvitations;
    private UserRepository userRepository;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_invitations);

        invitationsRecyclerView = findViewById(R.id.invitationsRecyclerView);
        tvNoInvitations = findViewById(R.id.tvNoInvitations);

        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        invitationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllianceInvitationsAdapter(new ArrayList<>(), new AllianceInvitationsAdapter.OnInvitationActionListener() {
            @Override
            public void onAccept(Alliance alliance) {
                acceptInvitation(alliance);
            }

            @Override
            public void onReject(Alliance alliance) {
                rejectInvitation(alliance);
            }
        });
        invitationsRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvitations();
    }

    private void loadInvitations() {
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getAllianceInvitationsReceived() != null && !user.getAllianceInvitationsReceived().isEmpty()) {
                    tvNoInvitations.setVisibility(View.GONE);
                    invitationsRecyclerView.setVisibility(View.VISIBLE);
                    userRepository.getAlliancesByIds(user.getAllianceInvitationsReceived(), new UserRepository.OnCompleteListener<List<Alliance>>() {
                        @Override
                        public void onSuccess(List<Alliance> alliances) {
                            adapter.setInvitations(alliances);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AllianceInvitationsActivity.this, "Error loading invitations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    tvNoInvitations.setVisibility(View.VISIBLE);
                    invitationsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllianceInvitationsActivity.this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptInvitation(Alliance alliance) {
        userRepository.acceptAllianceInvitation(currentUserId, alliance.getAllianceId(), new UserRepository.OnCompleteListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AllianceInvitationsActivity.this, "You have joined '" + alliance.getName() + "'!", Toast.LENGTH_SHORT).show();
                loadInvitations();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllianceInvitationsActivity.this, "Failed to accept invitation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rejectInvitation(Alliance alliance) {
        userRepository.rejectAllianceInvitation(currentUserId, alliance.getAllianceId(), new UserRepository.OnCompleteListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AllianceInvitationsActivity.this, "Invitation from '" + alliance.getName() + "' rejected.", Toast.LENGTH_SHORT).show();
                loadInvitations();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllianceInvitationsActivity.this, "Failed to reject invitation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}