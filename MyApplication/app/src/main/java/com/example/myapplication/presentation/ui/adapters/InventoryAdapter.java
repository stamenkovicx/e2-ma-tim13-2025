package com.example.myapplication.presentation.ui.adapters;

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
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.UserEquipment;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private Context context;
    private List<UserEquipment> userEquipmentList;
    private OnInventoryActionListener listener;

    public interface OnInventoryActionListener {
        void onActivateClick(UserEquipment userEquipment);
    }

    public InventoryAdapter(Context context, List<UserEquipment> userEquipmentList, OnInventoryActionListener listener) {
        this.context = context;
        this.userEquipmentList = userEquipmentList;
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
        UserEquipment userEquipment = userEquipmentList.get(position);

        // Dohvatanje Equipment objekta iz ItemRepository-a
        Equipment equipment = ItemRepository.getEquipmentById(userEquipment.getEquipmentId());

        if (equipment != null) {
            holder.tvItemName.setText(equipment.getName());
            holder.tvItemDescription.setText(equipment.getDescription());

            // Postavi sliku na osnovu IconResourceId iz Equipment klase
            int imageResId = context.getResources().getIdentifier(equipment.getIconResourceId(), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.ivItemImage.setImageResource(imageResId);
            } else {
                holder.ivItemImage.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Logika za prikaz stanja
            if (userEquipment.isActive()) {
                holder.ivActiveStatus.setVisibility(View.VISIBLE);
                holder.btnActivate.setVisibility(View.GONE); // Sakrij dugme ako je aktivno

                // Prikazi trajanje samo za opremu s trajanjem
                if (equipment.getDuration() > 0) {
                    holder.tvItemDuration.setVisibility(View.VISIBLE);
                    holder.tvItemDuration.setText("Remaining: " + userEquipment.getDuration() + " fight(s)");
                } else {
                    holder.tvItemDuration.setVisibility(View.GONE);
                }

            } else {
                holder.ivActiveStatus.setVisibility(View.GONE);
                holder.btnActivate.setVisibility(View.VISIBLE); // Prikazi dugme
                holder.tvItemDuration.setVisibility(View.GONE);
            }

            // Klik na dugme Activate
            holder.btnActivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivateClick(userEquipment);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userEquipmentList.size();
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