package com.example.myapplication.presentation.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.domain.models.MissionProgressItem;

import java.util.List;

public class MissionProgressAdapter extends RecyclerView.Adapter<MissionProgressAdapter.ViewHolder> {

    private List<MissionProgressItem> items;

    public MissionProgressAdapter(List<MissionProgressItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Povezuje se sa layoutom item_mission_progress.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MissionProgressItem item = items.get(position);

        // Postavljanje korisnickog imena i nanete stete
        holder.tvUsername.setText(item.getUsername());
        holder.tvDamage.setText(String.format("Šteta: %d", item.getDamageDealt()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /*
      Azurira listu stavki i osvezava prikaz.
     */
    public void setItems(List<MissionProgressItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // ViewHolder drzi reference na UI elemente unutar jedne stavke liste
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvDamage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvMemberUsername);
            tvDamage = itemView.findViewById(R.id.tvMemberDamage);
        }
    }
}