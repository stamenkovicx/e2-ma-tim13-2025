package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.EquipmentType;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.domain.models.UserEquipment;
import com.example.myapplication.presentation.ui.adapters.InventoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnInventoryActionListener {

    private RecyclerView rvPotions;
    private RecyclerView rvClothing;
    private RecyclerView rvWeapons;
    private Button btnBackToProfile;

    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        userRepository = new UserRepositoryFirebaseImpl();
        mAuth = FirebaseAuth.getInstance();

        rvPotions = findViewById(R.id.rvPotions);
        rvClothing = findViewById(R.id.rvClothing);
        rvWeapons = findViewById(R.id.rvWeapons);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        loadUserDataAndSetupInventory();

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void loadUserDataAndSetupInventory() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRepository.getUserById(firebaseUser.getUid(), new UserRepository.OnCompleteListener<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    currentUser = user;
                    setupInventoryLists();
                } else {
                    Toast.makeText(InventoryActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InventoryActivity.this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupInventoryLists() {
        if (currentUser == null || currentUser.getEquipment() == null || currentUser.getEquipment().isEmpty()) {
            // Ako je lista opreme prazna, prikazi poruku i nemoj postavljati adaptere
            Toast.makeText(this, "Your inventory is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<UserEquipment> allUserEquipment = currentUser.getEquipment();

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
        if (currentUser == null) return;

        // Pronalazimo pravi objekat opreme u globalnom user objektu
        int foundIndex = -1;
        for (int i = 0; i < currentUser.getEquipment().size(); i++) {
            if (currentUser.getEquipment().get(i).getEquipmentId() == userEquipment.getEquipmentId()) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex == -1) {
            Toast.makeText(this, "Item not found in inventory.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dobijamo referencu na objekat koji treba da modifikujemo
        final UserEquipment foundItem = currentUser.getEquipment().get(foundIndex);

        boolean isNowActive = !foundItem.isActive();
        foundItem.setActive(isNowActive);

        Equipment equipmentDetails = ItemRepository.getEquipmentById(foundItem.getEquipmentId());
        if (equipmentDetails == null) {
            Toast.makeText(this, "Equipment details not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ažuriranje PP-a
        double bonusPercentage = equipmentDetails.getBonusValue();
        final int currentPowerPoints = currentUser.getTotalPowerPoints();
        int basePowerPoints = currentUser.getPowerPoints();

        if (isNowActive) {
            int bonusToAdd = (int) (basePowerPoints * bonusPercentage);
            currentUser.setPowerPoints(currentPowerPoints + bonusToAdd);
            Toast.makeText(this, "Item '" + equipmentDetails.getName() + "' has been activated! Power has increased.", Toast.LENGTH_SHORT).show();
        } else {
            int bonusToRemove = (int) (basePowerPoints * bonusPercentage);
            currentUser.setPowerPoints(currentPowerPoints - bonusToRemove);
            Toast.makeText(this, "Item '" + equipmentDetails.getName() + "' has been deactivated! Power has been restored to normal.", Toast.LENGTH_SHORT).show();
        }

        // Ažuriranje korisnika u Firebase-u
        userRepository.updateUser(currentUser, new UserRepository.OnCompleteListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Ažuriraj listu nakon uspešnog ažuriranja u bazi
                setupInventoryLists();
            }

            @Override
            public void onFailure(Exception e) {
                // Vraćanje stanja u slučaju neuspešnog ažuriranja
                // Koristimo finalni foundItem objekat i finalnu currentPowerPoints vrednost
                foundItem.setActive(!isNowActive); // Vrati stanje nazad
                currentUser.setPowerPoints(currentPowerPoints); // Vrati PP nazad
                Toast.makeText(InventoryActivity.this, "Failed to update user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}