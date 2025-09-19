package com.example.myapplication.presentation.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;
import com.example.myapplication.domain.models.User;

import java.util.List;

public class AllianceInvitationsAdapter extends RecyclerView.Adapter<AllianceInvitationsAdapter.InvitationViewHolder> {

    private List<Alliance> invitations;
    private OnInvitationActionListener listener;
    private UserRepository userRepository;

    public interface OnInvitationActionListener {
        void onAccept(Alliance alliance);
        void onReject(Alliance alliance);
    }

    public AllianceInvitationsAdapter(List<Alliance> invitations, OnInvitationActionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
        this.userRepository = new UserRepositoryFirebaseImpl();
    }

    public void setInvitations(List<Alliance> invitations) {
        this.invitations = invitations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alliance_invitation, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        Alliance alliance = invitations.get(position);
        holder.tvAllianceName.setText(alliance.getName());

        userRepository.getUserById(alliance.getLeaderId(), new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User leader) {
                if (leader != null) {
                    holder.tvAllianceLeader.setText("From: " + leader.getUsername());
                } else {
                    holder.tvAllianceLeader.setText("From: Unknown User");
                }
            }

            @Override
            public void onFailure(Exception e) {
                holder.tvAllianceLeader.setText("From: Error loading user");
            }
        });

        // Postavljanje listenera za dugmad
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(alliance);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(alliance);
            }
        });
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    static class InvitationViewHolder extends RecyclerView.ViewHolder {
        TextView tvAllianceName, tvAllianceLeader;
        Button btnAccept;
        Button btnReject;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAllianceName = itemView.findViewById(R.id.tvAllianceName);
            btnAccept = itemView.findViewById(R.id.btnAcceptInvitation);
            btnReject = itemView.findViewById(R.id.btnRejectInvitation);
            tvAllianceLeader = itemView.findViewById(R.id.tvAllianceLeader);
        }
    }
}