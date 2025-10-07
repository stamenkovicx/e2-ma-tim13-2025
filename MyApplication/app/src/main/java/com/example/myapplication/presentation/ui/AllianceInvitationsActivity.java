package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.example.myapplication.domain.models.Notification;

import org.json.JSONArray;
import org.json.JSONObject;

public class AllianceInvitationsActivity extends AppCompatActivity {

    private RecyclerView invitationsRecyclerView;
    private AllianceInvitationsAdapter adapter;
    private TextView tvNoInvitations;
    private UserRepository userRepository;
    private String currentUserId;
    private String ONE_SIGNAL_REST_API_KEY = "os_v2_app_rbddxkp2tzhxrfkdkwjdt5rhllrqq2pdtnlu6f5xteinwr25gxkn2gq5olsnsqxpichbhkx2iytjrjbgaiqoxzwzbfybx4vz22xggja";


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
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User currentUser) {
                if (currentUser != null && currentUser.getAllianceId() != null && !currentUser.getAllianceId().isEmpty()) {
                    showSwitchAllianceDialog(currentUser, alliance);
                } else {
                    userRepository.acceptAllianceInvitation(currentUserId, alliance.getAllianceId(), new UserRepository.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AllianceInvitationsActivity.this, "You have joined '" + alliance.getName() + "'!", Toast.LENGTH_SHORT).show();

                            // SLANJE NOTIFIKACIJE LIDERU:
                            String leaderId = alliance.getLeaderId();
                            userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
                                @Override
                                public void onSuccess(User user) {
                                    if (user != null) {
                                        sendOneSignalNotification(user.getUsername(), leaderId);

                                        String notificationMessage = user.getUsername() + " has accepted your alliance invitation!";
                                        Notification notification = new Notification(
                                                leaderId,
                                                "alliance_invitation_accepted",
                                                notificationMessage,
                                                false,
                                                alliance.getAllianceId()
                                        );
                                        notification.setTimestamp(new Date());
                                        userRepository.addNotification(notification, new UserRepository.OnCompleteListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Uspješno kreirano, ali nema povratne informacije.
                                            }
                                            @Override
                                            public void onFailure(Exception e) {
                                                // Dodata toast poruka
                                                Toast.makeText(AllianceInvitationsActivity.this, "Failed to create notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    // Dodata toast poruka
                                    Toast.makeText(AllianceInvitationsActivity.this, "Failed to get accepting user data for notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                            loadInvitations();
                            Intent intent = new Intent(AllianceInvitationsActivity.this, AllianceActivity.class);
                            intent.putExtra("allianceId", alliance.getAllianceId()); // Koristi ID novog saveza
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AllianceInvitationsActivity.this, "Failed to accept invitation: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllianceInvitationsActivity.this, "Error checking user status: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




    private void sendOneSignalNotification(String userName, String leaderId) {
        new Thread(() -> {
            try {
                URL url = new URL("https://onesignal.com/api/v1/notifications");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);

                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Authorization", "Basic " + ONE_SIGNAL_REST_API_KEY);
                con.setRequestMethod("POST");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("app_id", "88463ba9-fa9e-4f78-9543-559239f6275a");

                JSONObject contents = new JSONObject();
                contents.put("en", userName + " has accepted your alliance invitation!");
                jsonBody.put("contents", contents);

                JSONObject headings = new JSONObject();
                headings.put("en", "Alliance notification");
                jsonBody.put("headings", headings);

                JSONArray filters = new JSONArray();

                // Filter: Ciljaj samo leader-a po user_id tagu
                JSONObject leaderFilter = new JSONObject();
                leaderFilter.put("field", "tag");
                leaderFilter.put("key", "user_id");
                leaderFilter.put("relation", "=");
                leaderFilter.put("value", leaderId);
                filters.put(leaderFilter);

                jsonBody.put("filters", filters);

                byte[] sendBytes = jsonBody.toString().getBytes("UTF-8");
                con.setFixedLengthStreamingMode(sendBytes.length);

                OutputStream os = con.getOutputStream();
                os.write(sendBytes);

                int httpResponse = con.getResponseCode();
                Log.d("OneSignal", "HTTP Response Code: " + httpResponse);

                String jsonResponse;
                if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                    Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                } else {
                    Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                }
                Log.d("OneSignal", "JSON Response: " + jsonResponse);

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }).start();
    }



    private void showSwitchAllianceDialog(User currentUser, Alliance newAlliance) {
        new AlertDialog.Builder(this)
                .setTitle("Switch Alliance?")
                .setMessage("You are already in an alliance. Joining '" + newAlliance.getName() + "' will make you leave your current alliance. Do you want to proceed?")
                .setPositiveButton("Proceed", (dialog, which) -> {
                    // Pozivamo metodu za prelazak na novi savez
                    switchAlliance(currentUser, newAlliance);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void switchAlliance(User currentUser, Alliance newAlliance) {
        userRepository.switchAlliance(currentUser.getUserId(), currentUser.getAllianceId(), newAlliance.getAllianceId(), new UserRepository.OnCompleteListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AllianceInvitationsActivity.this, "You have successfully switched to '" + newAlliance.getName() + "'!", Toast.LENGTH_SHORT).show();
                loadInvitations();

                Intent intent = new Intent(AllianceInvitationsActivity.this, AllianceActivity.class);
                intent.putExtra("allianceId", currentUser.getAllianceId());
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllianceInvitationsActivity.this, "Failed to switch alliance: " + e.getMessage(), Toast.LENGTH_LONG).show();
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