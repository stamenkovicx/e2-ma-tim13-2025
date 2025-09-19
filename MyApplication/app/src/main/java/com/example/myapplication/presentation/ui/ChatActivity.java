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
import java.util.ArrayList;
import java.util.List;

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

        // Dobivanje ID-a saveza iz Intent-a
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
                messagesRecyclerView.scrollToPosition(messages.size() - 1); // Skroluj do zadnje poruke
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}