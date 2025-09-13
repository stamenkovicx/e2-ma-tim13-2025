package com.example.myapplication.presentation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.DatabaseHelper;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private Context context;
    private List<Equipment> shopItems;
    private DatabaseHelper databaseHelper;
    private OnShopActionListener listener;
    public interface OnShopActionListener {
        void onCoinsUpdated(int newCoinValue);
    }

    public ShopAdapter(Context context, List<Equipment> shopItems, DatabaseHelper databaseHelper, OnShopActionListener listener) {
        this.context = context;
        this.shopItems = shopItems;
        this.databaseHelper = databaseHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_list, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Equipment currentItem = shopItems.get(position);
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return shopItems.size();
    }

    class ShopViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemIcon;
        TextView tvItemName, tvItemDescription, tvItemCost;
        Button btnBuy;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemIcon = itemView.findViewById(R.id.ivItemIcon);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemCost = itemView.findViewById(R.id.tvItemCost);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }

        public void bind(Equipment item) {
            // postavljanje detalja o item-u
            tvItemName.setText(item.getName());
            tvItemDescription.setText(item.getDescription());

            // postavljanje ikonice
            int resourceId = context.getResources().getIdentifier(item.getIconResourceId(), "drawable", context.getPackageName());
            if (resourceId != 0) {
                ivItemIcon.setImageResource(resourceId);
            }

            // hardkodovana nagrada za pobjedu bosa - treba dinamicki dobiti iz baze
            int potentialCoinsFromPreviousLevel = 240;
            final int finalCost;

            switch (item.getName()) {
                case "Minor Potion of Power":
                    finalCost = (int) (potentialCoinsFromPreviousLevel * 0.50);
                    break;
                case "Greater Potion of Power":
                    finalCost = (int) (potentialCoinsFromPreviousLevel * 0.70);
                    break;
                case "Elixir of Might":
                    finalCost = (int) (potentialCoinsFromPreviousLevel * 2.00);
                    break;
                case "Elixir of Greatness":
                    finalCost = (int) (potentialCoinsFromPreviousLevel * 10.00);
                    break;
                case "Gloves of Strength":
                case "Shield of Fortune":
                    finalCost = (int) (potentialCoinsFromPreviousLevel * 0.60);
                    break;
                case "Boots of Swiftness":
                    finalCost = (int) (potentialCoinsFromPreviousLevel * 0.80);
                    break;
                default:
                    finalCost = 0;
                    break;
            }

            tvItemCost.setText(String.valueOf(finalCost));

            btnBuy.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = databaseHelper.getUser(currentUser.getEmail());
                if (user == null) {
                    Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (user.getCoins() >= finalCost) {
                    user.setCoins(user.getCoins() - finalCost);
                    // Dodaj item u korpu
                    user.addEquipment(item.getIconResourceId());

                    databaseHelper.updateUser(user);

                    if (listener != null) {
                        listener.onCoinsUpdated(user.getCoins());
                    }

                    Toast.makeText(context, "You successfully bought " + item.getName() + "!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}