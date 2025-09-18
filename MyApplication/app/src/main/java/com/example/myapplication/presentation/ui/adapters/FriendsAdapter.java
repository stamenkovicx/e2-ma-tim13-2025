package com.example.myapplication.presentation.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.UserProfileActivity;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<User> friends;
    private User currentUser;
    private OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInviteClick(User friend);
    }

    public FriendsAdapter(List<User> friends, User currentUser, OnInviteClickListener listener) {
        this.friends = friends;
        this.currentUser = currentUser;
        this.listener = listener;
    }
    public void setFriends(List<User> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.bind(friend);
       // holder.tvFriendUsername.setText(friend.getUsername());

        // Logika za prikazivanje/skrivanje dugmeta "Invite to Alliance"
        if (currentUser.getAllianceId() != null && !currentUser.getAllianceId().isEmpty() &&
                (friend.getAllianceId() == null || friend.getAllianceId().isEmpty())) {

            // Provera da li je poziv već poslat
            if (currentUser.getAllianceInvitationsSent() != null && currentUser.getAllianceInvitationsSent().contains(friend.getUserId())) {
                holder.btnInviteFriend.setText("Invitation Sent");
                holder.btnInviteFriend.setEnabled(false);
                holder.btnInviteFriend.setVisibility(View.VISIBLE);
            } else {
                holder.btnInviteFriend.setText("Invite to Alliance");
                holder.btnInviteFriend.setEnabled(true);
                holder.btnInviteFriend.setVisibility(View.VISIBLE);

                holder.btnInviteFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onInviteClick(friend);
                    }
                });
            }
        } else {
            holder.btnInviteFriend.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView usernameTextView;
        private TextView levelTextView;
        private ImageView avatarImageView;
        private Button btnInviteFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textViewUsername);
            levelTextView = itemView.findViewById(R.id.textViewUserLevel);
            avatarImageView = itemView.findViewById(R.id.imageViewUserAvatar);
            btnInviteFriend = itemView.findViewById(R.id.btnInviteFriend);
            itemView.setOnClickListener(this);
        }

        public void bind(User friend) {
            usernameTextView.setText(friend.getUsername());
            levelTextView.setText("Level: " + friend.getLevel());
            // Postavite sliku avatara
            int avatarResourceId = itemView.getContext().getResources().getIdentifier(friend.getAvatar(), "drawable", itemView.getContext().getPackageName());
            avatarImageView.setImageResource(avatarResourceId);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                User clickedFriend = friends.get(position);
                Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                intent.putExtra("userId", clickedFriend.getUserId());
                v.getContext().startActivity(intent);
            }
        }
    }
}