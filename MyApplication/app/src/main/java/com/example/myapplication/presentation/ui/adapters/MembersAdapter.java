package com.example.myapplication.presentation.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.domain.models.User;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {

    private List<User> members;

    public MembersAdapter(List<User> members) {
        this.members = members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alliance_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = members.get(position);
        holder.tvMemberName.setText(member.getUsername());
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
        }
    }
}