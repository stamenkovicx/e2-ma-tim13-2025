package com.example.myapplication.presentation.ui.adapters;

import com.example.myapplication.R;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<User> users;
    private UserRepository userRepository;

    public UserSearchAdapter(List<User> users) {
        this.users = users;
        this.userRepository = new UserRepositoryFirebaseImpl();
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

    class UserViewHolder extends RecyclerView.ViewHolder {

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
        }

        public void bind(User user) {
            usernameTextView.setText(user.getUsername());
            levelTextView.setText("Level: " + user.getLevel());
            // TODO: Ucitati avatar slicicu, za sada koristimo placeholder
            // avatarImageView.setImageResource(R.mipmap.ic_launcher_round);

            addFriendButton.setOnClickListener(v -> {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String friendUserId = user.getUserId();

                userRepository.sendFriendRequest(currentUserId, friendUserId, new UserRepository.OnCompleteListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Toast.makeText(itemView.getContext(), "Friend request sent to " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        // Pošaljite notifikaciju ovde
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