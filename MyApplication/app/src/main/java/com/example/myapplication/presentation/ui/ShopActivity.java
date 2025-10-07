package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.repository.ItemRepository;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.domain.models.Equipment;
import com.example.myapplication.domain.models.EquipmentType;
import com.example.myapplication.domain.models.User;
import com.example.myapplication.presentation.ui.adapters.ShopAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity implements ShopAdapter.OnShopActionListener {

    private RecyclerView rvPotions;
    private RecyclerView rvClothing;
    private TextView tvUserCoins;
    private Button btnBackToProfile;

    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        userRepository = new UserRepositoryFirebaseImpl();
        mAuth = FirebaseAuth.getInstance();

        rvPotions = findViewById(R.id.rvPotions);
        rvClothing = findViewById(R.id.rvClothing);
        tvUserCoins = findViewById(R.id.tvUserCoins);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        loadUserDataAndSetupShop();

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void loadUserDataAndSetupShop() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userRepository.getUserById(firebaseUser.getUid(), new UserRepository.OnCompleteListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        currentUser = user;
                        updateCoinsDisplay();
                        setupShopLists();
                    } else {
                        Toast.makeText(ShopActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ShopActivity.this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupShopLists() {
        if (currentUser == null) {
            return;
        }

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

        // Postavljanje RecyclerView-a za napitke
        rvPotions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ShopAdapter potionsAdapter = new ShopAdapter(this, potions, currentUser, this);
        rvPotions.setAdapter(potionsAdapter);

        // Postavljanje RecyclerView-a za odeću
        rvClothing.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ShopAdapter clothingAdapter = new ShopAdapter(this, clothing, currentUser, this);
        rvClothing.setAdapter(clothingAdapter);
    }

    private void updateCoinsDisplay() {
        if (currentUser != null) {
            tvUserCoins.setText(String.valueOf(currentUser.getCoins()));
        }
    }

    @Override
    public void onCoinsUpdated(int newCoinValue) {
        updateCoinsDisplay();
    }
}