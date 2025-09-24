package com.example.myapplication.presentation.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.database.UserRepositoryFirebaseImpl;
import com.example.myapplication.data.repository.UserRepository;
import com.example.myapplication.domain.models.Alliance;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SpecialMissionActivity extends AppCompatActivity {

    // ✨ Reference na UI elemente, repozitorijum i tajmer ✨
    private TextView tvTimeRemaining, tvBossHp;
    private ProgressBar progressBossHp;
    private UserRepository userRepository;
    private String allianceId;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_mission);

        // Dobijamo ID alijanse koji je poslat iz AllianceActivity
        allianceId = getIntent().getStringExtra("allianceId");
        if (allianceId == null || allianceId.isEmpty()) {
            Toast.makeText(this, "Greška: ID Alijanse nije pronađen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicijalizacija
        userRepository = new UserRepositoryFirebaseImpl();
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        tvBossHp = findViewById(R.id.tvBossHp);
        progressBossHp = findViewById(R.id.progressBossHp);

        // Pozivamo metodu za učitavanje podataka
        loadMissionData();
    }

    // ✨ Metoda za učitavanje podataka o misiji iz baze ✨
    private void loadMissionData() {
        userRepository.getAllianceById(allianceId, new UserRepository.OnCompleteListener<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                if (alliance != null && alliance.isSpecialMissionActive()) {
                    updateBossHpUI(alliance.getSpecialMissionBossHp(), alliance.getSpecialMissionBossMaxHp());
                    startTimer(alliance.getSpecialMissionStartTime());
                } else {
                    tvBossHp.setText("Misija nije aktivna.");
                    progressBossHp.setVisibility(ProgressBar.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SpecialMissionActivity.this, "Greška pri učitavanju misije.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Pomocna metoda za azuriranje prikaza HP-a
    private void updateBossHpUI(int currentHp, int maxHp) {
        tvBossHp.setText(String.format("Boss HP: %d / %d", currentHp, maxHp));
        progressBossHp.setMax(maxHp);
        progressBossHp.setProgress(currentHp);
    }

    // Metoda koja pokrece tajmer
    private void startTimer(Date startTime) {
        // Misija traje 14 dana
        long missionDurationMillis = TimeUnit.DAYS.toMillis(14);
        long missionEndTimeMillis = startTime.getTime() + missionDurationMillis;
        long remainingTimeMillis = missionEndTimeMillis - System.currentTimeMillis();

        if (remainingTimeMillis <= 0) {
            tvTimeRemaining.setText("Misija je završena.");
            return;
        }

        countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                String timeText = String.format("Ostalo još: %d dana %02d:%02d:%02d", days, hours, minutes, seconds);
                tvTimeRemaining.setText(timeText);
            }

            @Override
            public void onFinish() {
                tvTimeRemaining.setText("Misija je završena.");
            }
        }.start();
    }

    //  Obavezno zaustavi tajmer kada se aktivnost unisti da se izbegnu problemi
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}