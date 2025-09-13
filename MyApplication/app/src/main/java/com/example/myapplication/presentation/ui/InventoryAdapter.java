package com.example.myapplication.presentation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.EquipmentType;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private Context context;
    private List<Equipment> equipmentList;
    private OnInventoryActionListener listener;

    public interface OnInventoryActionListener {
        void onActivateClick(Equipment equipment);
    }

    public InventoryAdapter(Context context, List<Equipment> equipmentList, OnInventoryActionListener listener) {
        this.context = context;
        this.equipmentList = equipmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);

        holder.tvItemName.setText(equipment.getName());
        holder.tvItemDescription.setText(equipment.getDescription());

        // Postavi sliku
        int imageResId = context.getResources().getIdentifier(equipment.getIconResourceId(), "drawable", context.getPackageName());
        if (imageResId != 0) {
            holder.ivItemImage.setImageResource(imageResId);
        } else {
            holder.ivItemImage.setImageResource(R.drawable.ic_launcher_foreground); // Rezervna slika
        }

        // Logika za prikaz stanja
        if (equipment.isActive()) {
            holder.ivActiveStatus.setVisibility(View.VISIBLE);
            holder.btnActivate.setEnabled(false); // Onemoguci dugme ako je vec aktivirano

            // Prikazi trajanje samo za odjecu i napitke
            if (equipment.getType() == EquipmentType.CLOTHING || equipment.getDuration() > 0) {
                holder.tvItemDuration.setVisibility(View.VISIBLE);
                holder.tvItemDuration.setText("Preostalo: " + equipment.getDuration() + " borbi");
            } else {
                holder.tvItemDuration.setVisibility(View.GONE);
            }

        } else {
            holder.ivActiveStatus.setVisibility(View.GONE);
            holder.btnActivate.setEnabled(true);
            holder.tvItemDuration.setVisibility(View.GONE);
        }

        // Klik na dugme Activate
        holder.btnActivate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActivateClick(equipment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDescription, tvItemDuration;
        ImageView ivItemImage, ivActiveStatus;
        Button btnActivate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemDuration = itemView.findViewById(R.id.tvItemDuration);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            ivActiveStatus = itemView.findViewById(R.id.ivActiveStatus);
            btnActivate = itemView.findViewById(R.id.btnActivate);
        }
    }
}