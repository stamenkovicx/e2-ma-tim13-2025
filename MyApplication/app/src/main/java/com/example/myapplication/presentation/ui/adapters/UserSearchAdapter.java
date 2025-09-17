package com.example.myapplication.presentation.ui.adapters;

import com.example.myapplication.R;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>  {

    private List<User> users;
    private UserRepository userRepository;
    private User currentUser;

    public UserSearchAdapter(List<User> users, User currentUser) {
        this.users = users;
        this.userRepository = new UserRepositoryFirebaseImpl();
        this.currentUser = currentUser;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView usernameTextView;
        private TextView levelTextView;
        private ImageView avatarImageView;
        private Button addFriendButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textViewUsername);
            levelTextView = itemView.findViewById(R.id.textViewUserLevel);
            avatarImageView = itemView.findViewById(R.id.imageViewUserAvatar);
            addFriendButton = itemView.findViewById(R.id.buttonAddFriend);

            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                User clickedUser = users.get(position);

                Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                intent.putExtra("userId", clickedUser.getUserId());
                v.getContext().startActivity(intent);
            }
        }
        public void bind(User user) {
            usernameTextView.setText(user.getUsername());
            levelTextView.setText("Level: " + user.getLevel());

            int avatarResourceId = itemView.getContext().getResources().getIdentifier(user.getAvatar(), "drawable", itemView.getContext().getPackageName());
            avatarImageView.setImageResource(avatarResourceId);

            // Provera da li je trenutni korisnik isti kao prikazani
            if (currentUser.getUserId().equals(user.getUserId())) {
                addFriendButton.setVisibility(View.GONE);
                return;
            } else {
                addFriendButton.setVisibility(View.VISIBLE);
            }

            // Provera da li je korisnik vec prijatelj
            if (currentUser.getFriends() != null && currentUser.getFriends().contains(user.getUserId())) {
                addFriendButton.setText(R.string.already_friends);
                addFriendButton.setEnabled(false);
            }
            // DODATO: Provera da li je trenutnom korisniku poslat zahtev od tog korisnika
            else if (currentUser.getFriendRequestsReceived() != null && currentUser.getFriendRequestsReceived().contains(user.getUserId())) {
                addFriendButton.setText(R.string.accept_friend);
                addFriendButton.setEnabled(true);
                addFriendButton.setOnClickListener(v -> {
                    // Logika za prihvatanje zahteva
                    userRepository.acceptFriendRequest(currentUser.getUserId(), user.getUserId(), new UserRepository.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(itemView.getContext(), "Friend request from " + user.getUsername() + " accepted!", Toast.LENGTH_SHORT).show();
                            addFriendButton.setText(R.string.already_friends);
                            addFriendButton.setEnabled(false);
                            // Azuriranje currentUser-a nakon prihvatanja zahtjeva
                            if (currentUser.getFriends() == null) {
                                currentUser.setFriends(new ArrayList<>());
                            }
                            currentUser.getFriends().add(user.getUserId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(itemView.getContext(), "Failed to accept friend request.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
            // Provera da li korisnik vec ima poslat zahtjev
            else if (currentUser.getFriendRequestsSent() != null && currentUser.getFriendRequestsSent().contains(user.getUserId())) {
                addFriendButton.setText(R.string.request_sent);
                addFriendButton.setEnabled(false);
            }
            // Standardno dugme za dodavanje prijatelja
            else {
                addFriendButton.setText(R.string.add_friend_button);
                addFriendButton.setEnabled(true);
                addFriendButton.setOnClickListener(v -> {
                    String currentUserId = currentUser.getUserId();
                    String friendUserId = user.getUserId();

                    userRepository.sendFriendRequest(currentUserId, friendUserId, new UserRepository.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(itemView.getContext(), "Friend request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();
                            addFriendButton.setText(R.string.request_sent);
                            addFriendButton.setEnabled(false);
                            if (currentUser.getFriendRequestsSent() == null) {
                                currentUser.setFriendRequestsSent(new ArrayList<>());
                            }
                            currentUser.getFriendRequestsSent().add(user.getUserId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(itemView.getContext(), "Failed to send friend request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        }
    }
}