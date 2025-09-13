package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.DatabaseHelper;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.EquipmentType;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.UserEquipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnInventoryActionListener {

    private RecyclerView rvPotions;
    private RecyclerView rvClothing;
    private RecyclerView rvWeapons;
    private Button btnBackToProfile;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        databaseHelper = new DatabaseHelper(this);

        rvPotions = findViewById(R.id.rvPotions);
        rvClothing = findViewById(R.id.rvClothing);
        rvWeapons = findViewById(R.id.rvWeapons);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        loadUserEquipment();

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void loadUserEquipment() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        User user = databaseHelper.getUser(currentUser.getEmail());
        if (user == null) {
            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<UserEquipment> allUserEquipment = user.getUserEquipmentList();

        List<UserEquipment> potions = new ArrayList<>();
        List<UserEquipment> clothing = new ArrayList<>();
        List<UserEquipment> weapons = new ArrayList<>();

        for (UserEquipment item : allUserEquipment) {
            Equipment equipmentDetails = ItemRepository.getEquipmentById(item.getEquipmentId());
            if (equipmentDetails != null) {
                if (equipmentDetails.getType() == EquipmentType.POTION) {
                    potions.add(item);
                } else if (equipmentDetails.getType() == EquipmentType.CLOTHING) {
                    clothing.add(item);
                } else if (equipmentDetails.getType() == EquipmentType.WEAPON) {
                    weapons.add(item);
                }
            }
        }

        rvPotions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        InventoryAdapter potionsAdapter = new InventoryAdapter(this, potions, this);
        rvPotions.setAdapter(potionsAdapter);

        rvClothing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        InventoryAdapter clothingAdapter = new InventoryAdapter(this, clothing, this);
        rvClothing.setAdapter(clothingAdapter);

        rvWeapons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        InventoryAdapter weaponsAdapter = new InventoryAdapter(this, weapons, this);
        rvWeapons.setAdapter(weaponsAdapter);
    }

    @Override
    public void onActivateClick(UserEquipment userEquipment) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        User user = databaseHelper.getUser(currentUser.getEmail());
        if (user == null) return;

        for (UserEquipment item : user.getUserEquipmentList()) {
            if (item.getEquipmentId() == userEquipment.getEquipmentId()) {
                item.setActive(true);
                break;
            }
        }

        databaseHelper.updateUser(user);

        // Prikaz poruke
        Equipment equipmentDetails = ItemRepository.getEquipmentById(userEquipment.getEquipmentId());
        if (equipmentDetails != null) {
            Toast.makeText(this, "Item '" + equipmentDetails.getName() + "' has been activated!", Toast.LENGTH_SHORT).show();
        }

        // Ponovo ucitavanje opreme da bi se azurirao prikaz
        loadUserEquipment();
    }
}