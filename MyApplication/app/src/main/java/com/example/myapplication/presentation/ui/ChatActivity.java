package com.example.myapplication.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Message;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.onesignal.OneSignal;
import org.json.JSONException;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private MessageAdapter messageAdapter;
    private final List<Message> messages = new ArrayList<>();
    private String allianceId;
    private String currentUserId;
    private String currentUserUsername;
    private FirebaseFirestore db;
    private UserRepository userRepository;
    private String ONE_SIGNAL_REST_API_KEY = "os_v2_app_rbddxkp2tzhxrfkdkwjdt5rhllrqq2pdtnlu6f5xteinwr25gxkn2gq5olsnsqxpichbhkx2iytjrjbgaiqoxzwzbfybx4vz22xggja";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        userRepository = new UserRepositoryFirebaseImpl();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Dobijnje ID-a saveza iz Intent-a
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("allianceId")) {
            allianceId = intent.getStringExtra("allianceId");
        } else {
            Toast.makeText(this, "Alliance ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicijalizacija RecyclerView-a i adaptera
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(this, messages);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Dohvatanje korisničkog imena za slanje poruka
        fetchCurrentUserData();

        // Slušanje poruka u realnom vremenu
        listenForMessages();

        // Slanje poruke
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void fetchCurrentUserData() {
        userRepository.getUserById(currentUserId, new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUserUsername = user.getUsername();
                    try {
                        JSONObject tags = new JSONObject();
                        tags.put("alliance_id", allianceId);
                        tags.put("user_id", currentUserId);
                        OneSignal.sendTags(tags);
                        Log.d("OneSignal", "Tags sent successfully: " + tags.toString());
                    } catch (Exception e) {
                        Log.e("OneSignal", "Failed to send tags: " + e.getMessage());
                    }
                } else {
                    Toast.makeText(ChatActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void listenForMessages() {
        CollectionReference chatRef = db.collection("alliances").document(allianceId).collection("messages");
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error loading messages: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (value != null) {
                List<Message> newMessages = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                    Message message = doc.toObject(Message.class);
                    if (message != null) {
                        message.setMessageId(doc.getId());
                        newMessages.add(message);
                    }
                }
                messages.clear();
                messages.addAll(newMessages);
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messages.size() - 1); // Skroluj do posljednje poruke
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty() || currentUserUsername == null) {
            return;
        }

        Message message = new Message(currentUserId, currentUserUsername, messageText, allianceId);
        db.collection("alliances").document(allianceId).collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    messageEditText.setText("");

                    new Thread(() -> {
                        try {
                            Thread.sleep(1500); // Odlozi 1.5 sekundi
                            sendOneSignalNotification(messageText);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    userRepository.applyDailyMessageBonus(allianceId, currentUserId, new UserRepository.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(ChatActivity.this, "Specijalni bonus primenjen!", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ChatActivity.this, "Greška pri primeni bonusa: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void sendOneSignalNotification(String messageText) {
        // Slanje notifikacije u pozadini niti
        new Thread(() -> {
            try {
                // URL za OneSignal API
                URL url = new URL("https://onesignal.com/api/v1/notifications");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);

                // POSTAVLJANJE ZAGLAVLJA ZA AUTORIZACIJU
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Authorization", "Basic " + ONE_SIGNAL_REST_API_KEY);
                con.setRequestMethod("POST");

                // Kreiranje JSON tijela zahtjeva
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("app_id", "88463ba9-fa9e-4f78-9543-559239f6275a");

                JSONObject contents = new JSONObject();
                contents.put("en", currentUserUsername + ": " + messageText);
                jsonBody.put("contents", contents);

                JSONObject headings = new JSONObject();
                headings.put("en", "New message in alliance!");
                jsonBody.put("headings", headings);

                JSONArray filters = new JSONArray();

                // Filter 1: Ciljanje svih korisnika u savezu
                JSONObject allianceFilter = new JSONObject();
                allianceFilter.put("field", "tag");
                allianceFilter.put("key", "alliance_id");
                allianceFilter.put("relation", "=");
                allianceFilter.put("value", allianceId);
                filters.put(allianceFilter);

                // Operator "AND"
                JSONObject operator = new JSONObject();
                operator.put("operator", "AND");
                filters.put(operator);

                // Filter 2: Isključivanje pošiljaoca
                JSONObject userFilter = new JSONObject();
                userFilter.put("field", "tag");
                userFilter.put("key", "user_id");
                userFilter.put("relation", "!=");
                userFilter.put("value", currentUserId);
                filters.put(userFilter);

                jsonBody.put("filters", filters);

                // Slanje zahtjeva
                byte[] sendBytes = jsonBody.toString().getBytes("UTF-8");
                con.setFixedLengthStreamingMode(sendBytes.length);

                OutputStream os = con.getOutputStream();
                os.write(sendBytes);

                int httpResponse = con.getResponseCode();
                Log.d("OneSignal", "HTTP Response Code: " + httpResponse);

                // Čitanje odgovora sa servera za debagovanje
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
}