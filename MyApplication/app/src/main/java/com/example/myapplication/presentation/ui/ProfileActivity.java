package com.example.myapplication.presentation.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.data.database.DatabaseHelper;
import com.example.myapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.graphics.Bitmap;
public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileAvatar;
    private TextView tvProfileUsername, tvLevel, tvTitle, tvPowerPoints, tvXP, tvCoins;
    private Button btnChangePassword;
    private ImageView ivQRCode;

    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicijalizacija baze podataka i Firebase-a
        databaseHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        // Povezivanje UI elemenata
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvLevel = findViewById(R.id.tvLevel);
        tvTitle = findViewById(R.id.tvTitle);
        tvPowerPoints = findViewById(R.id.tvPowerPoints);
        tvXP = findViewById(R.id.tvXP);
        tvCoins = findViewById(R.id.tvCoins);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        ivQRCode = findViewById(R.id.ivQRCode);

        // Ucitavanje korisnickih podataka
        loadUserProfileData();
    }

    private void loadUserProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();

            // Dohvatanje podataka iz lokalne baze
            User user = databaseHelper.getUser(email);

            if (user != null) {
                // Prikaz imena
                tvProfileUsername.setText(user.getUsername());

                // Prikaz avatara
                int avatarResourceId = getResources().getIdentifier(user.getAvatar(), "drawable", getPackageName());
                ivProfileAvatar.setImageResource(avatarResourceId);

                // Prikaz ostalih podataka
                tvLevel.setText(String.valueOf(user.getLevel()));
                tvTitle.setText(user.getTitle());
                tvPowerPoints.setText(String.valueOf(user.getPowerPoints()));
                tvXP.setText(String.valueOf(user.getXp()));
                tvCoins.setText(String.valueOf(user.getCoins()));

                // Podaci za QR kod:
                String qrData = "Username: " + user.getUsername() + "\n" +
                        "Level: " + user.getLevel() + "\n" +
                        "XP: " + user.getXp();
                // Generisanje i prikaz QR koda
                generateQRCode(qrData);

                // Ovdje ce kasnije ici logika za prikaz bedzeva i opreme

            } else {
                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQRCode(String data) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}