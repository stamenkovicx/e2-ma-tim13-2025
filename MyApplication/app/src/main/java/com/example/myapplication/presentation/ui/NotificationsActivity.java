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
import com.example.myapplication.domain.models.Notification;
import com.example.myapplication.presentation.ui.adapters.NotificationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private NotificationsAdapter adapter;
    private TextView tvNoNotifications;
    private UserRepository userRepository;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);

        userRepository = new UserRepositoryFirebaseImpl();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(new ArrayList<>(), notification -> {
            // Označi notifikaciju kao pročitanu kada se klikne na nju
            if (!notification.getIsRead()) {
                userRepository.markNotificationAsRead(notification.getNotificationId(), new UserRepository.OnCompleteListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        notification.setIsRead(true);
                        adapter.notifyDataSetChanged(); // Ažuriraj prikaz
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(NotificationsActivity.this, "Failed to mark as read.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        notificationsRecyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        userRepository.getAllNotifications(currentUserId, new UserRepository.OnCompleteListener<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (notifications != null && !notifications.isEmpty()) {
                    tvNoNotifications.setVisibility(View.GONE);
                    notificationsRecyclerView.setVisibility(View.VISIBLE);
                    adapter.setNotifications(notifications);
                } else {
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    notificationsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(NotificationsActivity.this, "Error loading notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();                tvNoNotifications.setVisibility(View.VISIBLE);
                notificationsRecyclerView.setVisibility(View.GONE);
            }
        });
    }
}