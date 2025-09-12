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
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileAvatar;
    private TextView tvProfileUsername, tvLevel, tvTitle, tvPowerPoints, tvXP, tvCoins;
    private Button btnChangePassword;
    private ImageView ivQRCode;
    private LinearLayout llBadgesContainer;
    private LinearLayout llEquipmentContainer;

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
        llBadgesContainer = findViewById(R.id.llBadgesContainer);
        llEquipmentContainer = findViewById(R.id.llEquipmentContainer);

        // Ucitavanje korisnickih podataka
        loadUserProfileData();

        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });
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

                // Bedzevi i oprema
                List<String> userBadges = user.getBadges();
                List<String> userEquipment = user.getEquipment();
                displayBadgesAndEquipment(userBadges, userEquipment);
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

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        final EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        final EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        final AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmNewPassword = etConfirmNewPassword.getText().toString();

            if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "New password cannot be empty.", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(ProfileActivity.this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
            } else {
                changePassword(oldPassword, newPassword, dialog);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void changePassword(String oldPassword, String newPassword, final AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Password successfully updated.", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Error updating password.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(ProfileActivity.this, "Wrong old password.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void displayBadgesAndEquipment(List<String> badges, List<String> equipment) {
        llBadgesContainer.removeAllViews();
        llEquipmentContainer.removeAllViews();

        for (String badgeName : badges) {
            ImageView badgeView = new ImageView(this);
            int resourceId = getResources().getIdentifier(badgeName, "drawable", getPackageName());
            if (resourceId != 0) {
                badgeView.setImageResource(resourceId);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                params.setMargins(0, 0, 16, 0);
                badgeView.setLayoutParams(params);
                llBadgesContainer.addView(badgeView);
            }
        }

        for (String equipmentName : equipment) {
            ImageView equipmentView = new ImageView(this);
            int resourceId = getResources().getIdentifier(equipmentName, "drawable", getPackageName());
            if (resourceId != 0) {
                equipmentView.setImageResource(resourceId);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                params.setMargins(0, 0, 16, 0);
                equipmentView.setLayoutParams(params);
                llEquipmentContainer.addView(equipmentView);
            }
        }
    }
}