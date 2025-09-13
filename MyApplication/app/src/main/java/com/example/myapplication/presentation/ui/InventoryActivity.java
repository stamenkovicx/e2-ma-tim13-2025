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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnInventoryActionListener {

    private RecyclerView rvPotions;
    private RecyclerView rvClothing;
    private RecyclerView rvWeapons;
    private Button btnBackToProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Povezivanje UI elemenata
        rvPotions = findViewById(R.id.rvPotions);
        rvClothing = findViewById(R.id.rvClothing);
        rvWeapons = findViewById(R.id.rvWeapons);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        // Učitavanje i prikaz opreme
        loadUserEquipment();

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void loadUserEquipment() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Ako korisnik nije ulogovan, vratite se na početni ekran
            Toast.makeText(this, "Korisnik nije ulogovan.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        // Pretpostavimo da getUserEquipment() metoda u DatabaseHelperu vraća svu opremu za tog korisnika.
        List<Equipment> userEquipment = databaseHelper.getUserEquipment(currentUser.getEmail());

        List<Equipment> potions = new ArrayList<>();
        List<Equipment> clothing = new ArrayList<>();
        List<Equipment> weapons = new ArrayList<>();

        for (Equipment item : userEquipment) {
            if (item.getType() == EquipmentType.POTION) {
                potions.add(item);
            } else if (item.getType() == EquipmentType.CLOTHING) {
                clothing.add(item);
            } else if (item.getType() == EquipmentType.WEAPON) {
                weapons.add(item);
            }
        }

        // Postavljanje RecyclerView-a za napitke
        rvPotions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        InventoryAdapter potionsAdapter = new InventoryAdapter(this, potions, this);
        rvPotions.setAdapter(potionsAdapter);

        // Postavljanje RecyclerView-a za odjecu
        rvClothing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        InventoryAdapter clothingAdapter = new InventoryAdapter(this, clothing, this);
        rvClothing.setAdapter(clothingAdapter);

        // Postavljanje RecyclerView-a za oruzje
        rvWeapons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        InventoryAdapter weaponsAdapter = new InventoryAdapter(this, weapons, this);
        rvWeapons.setAdapter(weaponsAdapter);
    }

    @Override
    public void onActivateClick(Equipment equipment) {
        // Ovde ide logika za aktivaciju opreme
        // Na primer, možete dodati bonus na korisnikove atribute
        if (equipment.getType() == EquipmentType.POTION) {
            Toast.makeText(this, "Napitak '" + equipment.getName() + "' je aktiviran!", Toast.LENGTH_SHORT).show();
        } else if (equipment.getType() == EquipmentType.CLOTHING) {
            Toast.makeText(this, "Odeća '" + equipment.getName() + "' je aktivirana!", Toast.LENGTH_SHORT).show();
        } else if (equipment.getType() == EquipmentType.WEAPON) {
            Toast.makeText(this, "Oružje '" + equipment.getName() + "' je aktivirano!", Toast.LENGTH_SHORT).show();
        }

        // U stvarnoj implementaciji, trebalo bi da ažurirate stanje opreme u bazi
        // npr. databaseHelper.activateEquipment(equipment.getId());
    }
}