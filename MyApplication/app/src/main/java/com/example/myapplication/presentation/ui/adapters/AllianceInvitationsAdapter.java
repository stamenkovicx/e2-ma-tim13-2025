package com.example.myapplication.presentation.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.domain.models.Alliance;
import java.util.List;

public class AllianceInvitationsAdapter extends RecyclerView.Adapter<AllianceInvitationsAdapter.InvitationViewHolder> {

    private List<Alliance> invitations;
    private OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onAccept(Alliance alliance);
        void onReject(Alliance alliance);
    }

    public AllianceInvitationsAdapter(List<Alliance> invitations, OnInvitationActionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
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
        TextView tvAllianceName;
        Button btnAccept;
        Button btnReject;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAllianceName = itemView.findViewById(R.id.tvAllianceName);
            btnAccept = itemView.findViewById(R.id.btnAcceptInvitation);
            btnReject = itemView.findViewById(R.id.btnRejectInvitation);
        }
    }
}