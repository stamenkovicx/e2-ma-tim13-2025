package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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

public class ShopActivity extends AppCompatActivity implements ShopAdapter.OnShopActionListener {

    private RecyclerView rvPotions;
    private RecyclerView rvClothing;
    private TextView tvUserCoins;
    private Button btnBackToProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Povezivanje UI elemenata
        rvPotions = findViewById(R.id.rvPotions);
        rvClothing = findViewById(R.id.rvClothing);
        tvUserCoins = findViewById(R.id.tvUserCoins);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        // Postavljanje lista za prodavnicu
        setupShopLists();

        // Prikaz novcica korisnika (za sada je hardkodovano)
        tvUserCoins.setText("500");

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void setupShopLists() {
        List<Equipment> allEquipment = ItemRepository.getAllEquipment();
        List<Equipment> potions = new ArrayList<>();
        List<Equipment> clothing = new ArrayList<>();

        for (Equipment item : allEquipment) {
            if (item.getType() == EquipmentType.POTION) {
                potions.add(item);
            } else if (item.getType() == EquipmentType.CLOTHING) {
                clothing.add(item);
            }
        }

        // Povezivanje sa bazom podataka
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        // Postavljanje RecyclerView-a za napitke
        rvPotions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ShopAdapter potionsAdapter = new ShopAdapter(this, potions, databaseHelper, this);
        rvPotions.setAdapter(potionsAdapter);

        // Postavljanje RecyclerView-a za odjecu
        rvClothing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ShopAdapter clothingAdapter = new ShopAdapter(this, clothing, databaseHelper, this);
        rvClothing.setAdapter(clothingAdapter);

        updateCoinsDisplay();
    }

    // Azuriranje prikaza novcica
    private void updateCoinsDisplay() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            User user = new DatabaseHelper(this).getUser(currentUser.getEmail());
            if (user != null) {
                tvUserCoins.setText(String.valueOf(user.getCoins()));
            }
        }
    }

    @Override
    public void onCoinsUpdated(int newCoinValue) {
        // Ova metoda ce biti pozvana iz adaptera nakon uspjesne kupovine
        tvUserCoins.setText(String.valueOf(newCoinValue));
    }
}